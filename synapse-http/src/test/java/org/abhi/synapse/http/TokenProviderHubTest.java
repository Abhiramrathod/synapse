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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenProviderHubTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
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
    void configWithTokenProviderWithoutApiKeyIsValid() throws Exception {
        mockWebServer.enqueue(openAiCompletion("ok"));

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .tokenProvider(TokenProvider.fromSupplier(() -> "managed-identity-token"))
                .modelName("gpt-4")
                .maxRetries(0)
                .build();

        try (SynapseHub hub = new SynapseHub(config)) {
            SynapseResponse response = hub.sendPrompt("hi", null);
            assertThat(response.getContent()).isEqualTo("ok");

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getHeader("Authorization")).isEqualTo("Bearer managed-identity-token");
        }
    }

    @Test
    void configWithoutAnyCredentialIsRejected() {
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .modelName("gpt-4")
                .maxRetries(0)
                .build();

        assertThatThrownBy(() -> new SynapseHub(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("apiKey is required");
    }

    @Test
    void tokenProviderIsInvokedForEveryRequest() throws Exception {
        mockWebServer.enqueue(openAiCompletion("a"));
        mockWebServer.enqueue(openAiCompletion("b"));

        AtomicInteger calls = new AtomicInteger();
        TokenProvider provider = TokenProvider.fromSupplier(() -> "token-" + calls.incrementAndGet());

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .tokenProvider(provider)
                .modelName("gpt-4")
                .maxRetries(0)
                .build();

        try (SynapseHub hub = new SynapseHub(config)) {
            hub.sendPrompt("hi", null);
            hub.sendPrompt("hi", null);
        }

        assertThat(mockWebServer.takeRequest().getHeader("Authorization")).isEqualTo("Bearer token-1");
        assertThat(mockWebServer.takeRequest().getHeader("Authorization")).isEqualTo("Bearer token-2");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void supplierAliasBuildsBearerHeader() {
        TokenProvider provider = TokenProvider.fromSupplier(() -> "supplied-token");
        assertThat(provider.buildAuthorizationHeader()).isEqualTo("Bearer supplied-token");
    }

    @Test
    void supplierAliasIsUsedByHub() throws Exception {
        mockWebServer.enqueue(openAiCompletion("ok"));

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .tokenProvider(TokenProvider.fromSupplier(() -> "supplied-token"))
                .modelName("gpt-4")
                .maxRetries(0)
                .build();

        try (SynapseHub hub = new SynapseHub(config)) {
            hub.sendPrompt("hi", null);
            assertThat(mockWebServer.takeRequest().getHeader("Authorization")).isEqualTo("Bearer supplied-token");
        }
    }
}
