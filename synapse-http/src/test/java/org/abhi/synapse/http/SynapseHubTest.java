package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseHubTest {

    private MockWebServer mockWebServer;
    private SynapseHub hub;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test-key")
                .modelName("gpt-4")
                .temperature(0.7)
                .maxTokens(100)
                .enableLogging(true)
                .maxRetries(0)
                .build();

        hub = new SynapseHub(config);
    }

    @AfterEach
    void tearDown() throws Exception {
        hub.close();
        mockWebServer.shutdown();
    }

    @Test
    void sendPromptSuccess() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-1","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"Hello!"},"finish_reason":"stop"}],"usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}
                        """)
                .addHeader("Content-Type", "application/json"));

        SynapseResponse response = hub.sendPrompt("Hi there", null);

        assertThat(response.getContent()).isEqualTo("Hello!");
        assertThat(response.getModel()).isEqualTo("gpt-4");
        assertThat(response.getPromptTokens()).isEqualTo(10);
        assertThat(response.getCompletionTokens()).isEqualTo(5);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v1/chat/completions");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer sk-test-key");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
    }

    @Test
    void sendChatSuccess() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-2","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"Java is a programming language."},"finish_reason":"stop"}],"usage":{"prompt_tokens":20,"completion_tokens":8,"total_tokens":28}}
                        """)
                .addHeader("Content-Type", "application/json"));

        List<ChatMessage> messages = List.of(
                ChatMessage.system("You are helpful"),
                ChatMessage.user("What is Java?")
        );
        SynapseResponse response = hub.sendChat(messages, null);

        assertThat(response.getContent()).isEqualTo("Java is a programming language.");
        assertThat(response.getPromptTokens()).isEqualTo(20);
        assertThat(response.getCompletionTokens()).isEqualTo(8);
    }

    @Test
    void http429ThrowsRateLimitException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("Rate limit exceeded"));

        assertThatThrownBy(() -> hub.sendPrompt("test", null))
                .isInstanceOf(SynapseException.class)
                .satisfies(e -> assertThat(((SynapseException) e).getStatusCode()).isEqualTo(429));
    }

    @Test
    void http500ThrowsServerErrorException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThatThrownBy(() -> hub.sendPrompt("test", null))
                .isInstanceOf(SynapseException.class)
                .satisfies(e -> assertThat(((SynapseException) e).getStatusCode()).isEqualTo(500));
    }

    @Test
    void hubRejectsRequestsAfterClose() {
        hub.close();
        assertThatThrownBy(() -> hub.sendPrompt("test", null))
                .isInstanceOf(SynapseException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void asyncSendPromptReturnsCompletableFuture() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-3","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"Async response"},"finish_reason":"stop"}],"usage":{"prompt_tokens":5,"completion_tokens":3,"total_tokens":8}}
                        """)
                .addHeader("Content-Type", "application/json"));

        CompletableFuture<SynapseResponse> future = hub.sendPromptAsync("Hello async", null);
        SynapseResponse response = future.get();

        assertThat(response.getContent()).isEqualTo("Async response");
    }

    @Test
    void modelOverrideViaRequestOptions() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-4","object":"chat.completion","model":"gpt-3.5-turbo","choices":[{"index":0,"message":{"role":"assistant","content":"response"},"finish_reason":"stop"}],"usage":{"prompt_tokens":5,"completion_tokens":2,"total_tokens":7}}
                        """)
                .addHeader("Content-Type", "application/json"));

        RequestOptions opts = RequestOptions.defaults().setModelName("gpt-3.5-turbo");
        SynapseResponse response = hub.sendPrompt("test", opts);
        assertThat(response.getContent()).isEqualTo("response");
    }

    @Test
    void getModelsListReturnsModels() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"object":"list","data":[{"id":"gpt-4","object":"model","created":1687100000,"owned_by":"openai"},{"id":"gpt-3.5-turbo","object":"model","created":1687000000,"owned_by":"openai"}]}
                        """)
                .addHeader("Content-Type", "application/json"));

        var models = hub.getModelsList();
        assertThat(models).hasSize(2);
        assertThat(models.get(0).getId()).isEqualTo("gpt-4");
        assertThat(models.get(1).getId()).isEqualTo("gpt-3.5-turbo");
    }
}
