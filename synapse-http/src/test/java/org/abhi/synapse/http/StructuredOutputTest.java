package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.ProviderAdapter;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredOutputTest {

    private MockWebServer mockWebServer;
    private SynapseHub hub;

    record Person(String name, int age) {
    }

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test-key")
                .modelName("gpt-4")
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
    void parsesStructuredResponseIntoType() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"cmpl-so","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"{\\"name\\":\\"Alice\\",\\"age\\":30}"},"finish_reason":"stop"}],"usage":{"prompt_tokens":20,"completion_tokens":8,"total_tokens":28}}
                        """)
                .addHeader("Content-Type", "application/json"));

        Person person = hub.sendPrompt("Extract the person", Person.class, null);

        assertThat(person.name()).isEqualTo("Alice");
        assertThat(person.age()).isEqualTo(30);

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"response_format\"");
        assertThat(body).contains("\"json_schema\"");
        assertThat(body).contains("\"name\":\"Person\"");
        assertThat(body).contains("\"properties\"");
    }

    @Test
    void fallsBackToPromptInjectionWhenProviderLacksSupport() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"cmpl-so2","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"{\\"name\\":\\"Bob\\",\\"age\\":42}"},"finish_reason":"stop"}],"usage":{"prompt_tokens":20,"completion_tokens":8,"total_tokens":28}}
                        """)
                .addHeader("Content-Type", "application/json"));

        SynapseHub noNative = new SynapseHub(SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/custom/chat")
                .apiKey("sk-test-key")
                .modelName("custom-model")
                .provider(fallbackAdapter())
                .maxRetries(0)
                .build());
        hub.close();
        hub = noNative;

        Person person = hub.sendPrompt("Extract the person", Person.class, null);

        assertThat(person.name()).isEqualTo("Bob");
        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();
        assertThat(body).doesNotContain("\"response_format\"");
        assertThat(body).contains("matching this JSON Schema");
        assertThat(body).contains("properties");
        assertThat(body).contains("\\\"name\\\":{\\\"type\\\":\\\"string\\\"}");
    }

    @Test
    void rejectsScalarReturnTypes() {
        assertThatThrownBy(() -> hub.sendPrompt("count", Integer.class, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("POJO or record");
    }

    private static ProviderAdapter fallbackAdapter() {
        return new ProviderAdapter() {
            @Override public String providerName() { return "fallback"; }
            @Override public String buildUrl(String baseUrl, String endpoint) { return baseUrl.replaceAll("/+$", "") + endpoint; }
            @Override public Map<String, String> buildAuthHeaders(String apiKey) { return Map.of(); }
            @Override public Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
                    int maxTokens, String modelName, boolean streaming, List<ToolDefinition> tools, String responseFormat) {
                return Map.of("model", modelName, "messages", messages);
            }
            @Override public SynapseResponse parseResponse(String responseBody) {
                SynapseResponse r = new SynapseResponse();
                r.setContent("{\"name\":\"Bob\",\"age\":42}");
                return r;
            }
            @Override public List<Model> parseModels(String responseBody) { return List.of(); }
            @Override public String extractContentFromStreamChunk(String jsonData) { return ""; }
            @Override public boolean isStreamDone(String line) { return true; }
            @Override public boolean supportsJsonSchemaStructuredOutput() { return false; }
        };
    }
}
