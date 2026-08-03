package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.StreamHandle;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.ToolDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    void requestLevelVariablesAreRenderedIntoMessages() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-5","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"ok"},"finish_reason":"stop"}],"usage":{"prompt_tokens":5,"completion_tokens":2,"total_tokens":7}}
                        """)
                .addHeader("Content-Type", "application/json"));

        RequestOptions opts = RequestOptions.defaults()
                .setVariables(Map.of("name", "Alice", "day", "Monday"));
        hub.sendChat(List.of(ChatMessage.user("Hello {name}, how is {day}?")), opts);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getBody().readUtf8()).contains("Hello Alice, how is Monday?");
    }

    @Test
    void declarativeToolCallRunsLoopToCompletion() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-10","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":null,"tool_calls":[{"id":"call_1","type":"function","function":{"name":"getWeather","arguments":"{\\"city\\":\\"Paris\\"}"}}]},"finish_reason":"tool_calls"}],"usage":{"prompt_tokens":30,"completion_tokens":10,"total_tokens":40}}
                        """)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-11","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"It is Sunny in Paris"},"finish_reason":"stop"}],"usage":{"prompt_tokens":40,"completion_tokens":12,"total_tokens":52}}
                        """)
                .addHeader("Content-Type", "application/json"));

        RequestOptions opts = RequestOptions.defaults()
                .setToolInstances(List.of(new ToolRegistryTest.WeatherTools()));

        SynapseResponse response = hub.sendChat(
                List.of(ChatMessage.user("What is the weather in Paris?")), opts);

        assertThat(response.getContent()).isEqualTo("It is Sunny in Paris");

        RecordedRequest first = mockWebServer.takeRequest();
        assertThat(first.getBody().readUtf8()).contains("\"tools\"");

        RecordedRequest second = mockWebServer.takeRequest();
        String secondBody = second.getBody().readUtf8();
        assertThat(secondBody).contains("\"role\":\"tool\"");
        assertThat(secondBody).contains("\"tool_call_id\":\"call_1\"");
        assertThat(secondBody).contains("Sunny in Paris");
    }

    @Test
    void injectedProviderAdapterIsUsed() throws Exception {
        ProviderAdapter custom = new ProviderAdapter() {
            @Override public String providerName() { return "custom"; }
            @Override public String buildUrl(String baseUrl, String endpoint) { return baseUrl.replaceAll("/+$", "") + "/custom/chat"; }
            @Override public String buildModelsUrl(String baseUrl) { return baseUrl.replaceAll("/+$", "") + "/custom/models"; }
            @Override public Map<String, String> buildAuthHeaders(String apiKey) { return Map.of("X-Custom-Key", apiKey); }
            @Override public Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
                    int maxTokens, String modelName, boolean streaming, List<ToolDefinition> tools, String responseFormat) {
                return Map.of("model", modelName, "messages", messages, "custom", true);
            }
            @Override public SynapseResponse parseResponse(String responseBody) {
                SynapseResponse r = new SynapseResponse();
                r.setContent("parsed-by-custom-adapter");
                return r;
            }
            @Override public List<Model> parseModels(String responseBody) { return List.of(); }
            @Override public String extractContentFromStreamChunk(String jsonData) { return ""; }
            @Override public boolean isStreamDone(String line) { return true; }
        };

        SynapseConfig customConfig = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test-key")
                .modelName("custom-model")
                .provider(custom)
                .enableLogging(true)
                .maxRetries(0)
                .build();
        hub.close();
        hub = new SynapseHub(customConfig);

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"custom\":true}")
                .addHeader("Content-Type", "application/json"));

        SynapseResponse response = hub.sendPrompt("hello", null);

        assertThat(response.getContent()).isEqualTo("parsed-by-custom-adapter");
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/custom/chat");
        assertThat(request.getHeader("X-Custom-Key")).isEqualTo("sk-test-key");
    }

    @Test
    void streamingUsageChunkPopulatesTokenCounts() throws Exception {
        String sse = ""
                + "data: {\"id\":\"1\",\"object\":\"chat.completion.chunk\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hel\"}}]}\n\n"
                + "data: {\"id\":\"1\",\"object\":\"chat.completion.chunk\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"lo!\"}}]}\n\n"
                + "data: {\"id\":\"1\",\"object\":\"chat.completion.chunk\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n"
                + "data: {\"id\":\"1\",\"object\":\"chat.completion.chunk\",\"choices\":[],\"usage\":{\"prompt_tokens\":42,\"completion_tokens\":7,\"total_tokens\":49}}\n\n"
                + "data: [DONE]\n\n";
        mockWebServer.enqueue(new MockResponse()
                .setBody(sse)
                .addHeader("Content-Type", "text/event-stream"));

        AtomicReference<String> content = new AtomicReference<>("");
        AtomicReference<SynapseResponse> completed = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        hub.streamChat(List.of(ChatMessage.user("hi")), new StreamListener() {
            @Override public void onChunk(String chunk) { content.set(content.get() + chunk); }
            @Override public void onComplete(SynapseResponse response) { completed.set(response); done.countDown(); }
            @Override public void onError(SynapseException error) { done.countDown(); }
        });

        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(content.get()).isEqualTo("Hello!");
        SynapseResponse response = completed.get();
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello!");
        assertThat(response.getPromptTokens()).isEqualTo(42);
        assertThat(response.getCompletionTokens()).isEqualTo(7);
    }
}
