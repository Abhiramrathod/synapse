package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.TokenProvider;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicReconfigurationTest {

    private MockWebServer primaryServer;
    private MockWebServer secondaryServer;
    private SynapseHub hub;

    @BeforeEach
    void setUp() throws Exception {
        primaryServer = new MockWebServer();
        primaryServer.start();
        secondaryServer = new MockWebServer();
        secondaryServer.start();

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(primaryServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-original")
                .modelName("gpt-4")
                .temperature(0.7)
                .maxTokens(100)
                .maxRetries(0)
                .build();

        hub = new SynapseHub(config);
    }

    @AfterEach
    void tearDown() throws Exception {
        hub.close();
        primaryServer.shutdown();
        secondaryServer.shutdown();
    }

    private static MockResponse openAiCompletion(String content) {
        return new MockResponse()
                .setBody("{\"id\":\"chatcmpl-x\",\"object\":\"chat.completion\",\"model\":\"m\","
                        + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\""
                        + content + "\"},\"finish_reason\":\"stop\"}],"
                        + "\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":5,\"total_tokens\":15}}")
                .addHeader("Content-Type", "application/json");
    }

    @Test
    void rotatingApiKeyAppliesToSubsequentRequests() throws Exception {
        primaryServer.enqueue(openAiCompletion("first"));
        primaryServer.enqueue(openAiCompletion("second"));

        hub.sendPrompt("hello", null);
        hub.updateApiKey("sk-rotated");
        hub.sendPrompt("hello again", null);

        RecordedRequest first = primaryServer.takeRequest();
        RecordedRequest second = primaryServer.takeRequest();
        assertThat(first.getHeader("Authorization")).isEqualTo("Bearer sk-original");
        assertThat(second.getHeader("Authorization")).isEqualTo("Bearer sk-rotated");
    }

    @Test
    void switchingDefaultModelChangesRequestBody() throws Exception {
        primaryServer.enqueue(openAiCompletion("a"));
        primaryServer.enqueue(openAiCompletion("b"));

        hub.sendPrompt("hi", null);
        hub.updateDefaultModel("gpt-4o");
        hub.sendPrompt("hi", null);

        RecordedRequest first = primaryServer.takeRequest();
        RecordedRequest second = primaryServer.takeRequest();
        assertThat(first.getBody().readUtf8()).contains("\"model\":\"gpt-4\"");
        assertThat(second.getBody().readUtf8()).contains("\"model\":\"gpt-4o\"");
    }

    @Test
    void switchingBaseUrlReroutesRequestsWithoutHubRecreation() throws Exception {
        primaryServer.enqueue(openAiCompletion("old"));
        secondaryServer.enqueue(openAiCompletion("new"));

        SynapseResponse first = hub.sendPrompt("hi", null);
        hub.updateBaseUrl(secondaryServer.url("/").toString().replaceAll("/$", ""));
        SynapseResponse second = hub.sendPrompt("hi", null);

        assertThat(first.getContent()).isEqualTo("old");
        assertThat(second.getContent()).isEqualTo("new");
        assertThat(primaryServer.getRequestCount()).isEqualTo(1);
        assertThat(secondaryServer.getRequestCount()).isEqualTo(1);
        assertThat(secondaryServer.takeRequest().getHeader("Authorization")).isEqualTo("Bearer sk-original");
    }

    @Test
    void switchingEndpointChangesRequestPath() throws Exception {
        primaryServer.enqueue(openAiCompletion("a"));

        hub.updateEndpoint("/v2/completions");
        hub.sendPrompt("hi", null);

        assertThat(primaryServer.takeRequest().getPath()).isEqualTo("/v2/completions");
    }

    @Test
    void updatingTemperatureAndMaxTokensChangesRequestBody() throws Exception {
        primaryServer.enqueue(openAiCompletion("a"));
        primaryServer.enqueue(openAiCompletion("b"));

        hub.sendPrompt("hi", null);
        hub.updateTemperature(0.2);
        hub.updateMaxTokens(512);
        hub.sendPrompt("hi", null);

        RecordedRequest first = primaryServer.takeRequest();
        RecordedRequest second = primaryServer.takeRequest();
        assertThat(first.getBody().readUtf8())
                .contains("\"temperature\":0.7").contains("\"max_tokens\":100");
        assertThat(second.getBody().readUtf8())
                .contains("\"temperature\":0.2").contains("\"max_tokens\":512");
    }

    @Test
    void updatingRequestTimeoutIsAccepted() throws Exception {
        primaryServer.enqueue(openAiCompletion("a"));

        hub.updateRequestTimeout(Duration.ofSeconds(30));
        hub.sendPrompt("hi", null);

        assertThat(primaryServer.takeRequest().getPath()).isEqualTo("/v1/chat/completions");
    }

    @Test
    void reconfigureAppliesDynamicFieldsFromNewConfig() throws Exception {
        primaryServer.enqueue(openAiCompletion("before"));
        primaryServer.enqueue(openAiCompletion("after"));

        hub.sendPrompt("hi", null);

        SynapseConfig replacement = SynapseConfig.builder()
                .baseUrl(primaryServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v2/chat")
                .apiKey("sk-reconfigured")
                .modelName("gpt-5")
                .temperature(0.1)
                .maxTokens(2048)
                .maxRetries(0)
                .build();
        hub.reconfigure(replacement);
        hub.sendPrompt("hi", null);

        RecordedRequest first = primaryServer.takeRequest();
        RecordedRequest second = primaryServer.takeRequest();
        assertThat(first.getHeader("Authorization")).isEqualTo("Bearer sk-original");
        assertThat(first.getBody().readUtf8()).contains("\"model\":\"gpt-4\"");
        assertThat(second.getHeader("Authorization")).isEqualTo("Bearer sk-reconfigured");
        assertThat(second.getPath()).isEqualTo("/v2/chat");
        assertThat(second.getBody().readUtf8())
                .contains("\"model\":\"gpt-5\"").contains("\"temperature\":0.1").contains("\"max_tokens\":2048");
    }

    @Test
    void updateTokenProviderSwitchesAuthenticationForLaterRequests() throws Exception {
        primaryServer.enqueue(openAiCompletion("a"));
        primaryServer.enqueue(openAiCompletion("b"));

        AtomicReference<String> token = new AtomicReference<>("static-token");
        hub.sendPrompt("hi", null);
        hub.updateTokenProvider(TokenProvider.fromSupplier(token::get));
        token.set("rotated-token");
        hub.sendPrompt("hi", null);

        RecordedRequest first = primaryServer.takeRequest();
        RecordedRequest second = primaryServer.takeRequest();
        assertThat(first.getHeader("Authorization")).isEqualTo("Bearer sk-original");
        assertThat(second.getHeader("Authorization")).isEqualTo("Bearer rotated-token");
    }

    @Test
    void asyncRequestsUseUpdatedSettings() throws Exception {
        primaryServer.enqueue(openAiCompletion("async-before"));
        primaryServer.enqueue(openAiCompletion("async-after"));

        hub.sendPromptAsync("hi", null).get(10, TimeUnit.SECONDS);
        hub.updateDefaultModel("gpt-4-turbo");
        hub.sendPromptAsync("hi", null).get(10, TimeUnit.SECONDS);

        RecordedRequest first = primaryServer.takeRequest();
        RecordedRequest second = primaryServer.takeRequest();
        assertThat(first.getBody().readUtf8()).contains("\"model\":\"gpt-4\"");
        assertThat(second.getBody().readUtf8()).contains("\"model\":\"gpt-4-turbo\"");
    }

    @Test
    void updateMethodsRejectInvalidInput() {
        assertThatThrownBy(() -> hub.updateApiKey("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hub.updateDefaultModel("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hub.updateBaseUrl("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hub.updateEndpoint(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hub.updateRequestTimeout(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hub.updateMaxTokens(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> hub.updateTokenProvider(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateMethodsRejectAfterClose() {
        hub.close();
        assertThatThrownBy(() -> hub.updateApiKey("sk-new")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> hub.updateDefaultModel("gpt-4o")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> hub.updateBaseUrl("http://localhost:9")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reconfigureRejectsInvalidConfig() {
        SynapseConfig missingModel = SynapseConfig.builder()
                .baseUrl("http://localhost:9")
                .endpoint("/v1/chat/completions")
                .apiKey("sk")
                .maxRetries(0)
                .build();
        assertThatThrownBy(() -> hub.reconfigure(missingModel)).isInstanceOf(IllegalArgumentException.class);
    }
}
