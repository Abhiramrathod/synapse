package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.cache.ResponseCache;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseCacheHubTest {

    private MockWebServer mockWebServer;
    private MapResponseCache cache;
    private SynapseHub hub;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        cache = new MapResponseCache();

        SynapseConfig config = SynapseConfig.builder()
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test-key")
                .modelName("gpt-4")
                .maxRetries(0)
                .cache(cache)
                .build();

        hub = new SynapseHub(config);
    }

    @AfterEach
    void tearDown() throws Exception {
        hub.close();
        mockWebServer.shutdown();
    }

    @Test
    void repeatedPromptIsServedFromCacheWithoutHittingProvider() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"chatcmpl-1","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"Cached answer"},"finish_reason":"stop"}],"usage":{"prompt_tokens":5,"completion_tokens":5,"total_tokens":10}}
                        """)
                .addHeader("Content-Type", "application/json"));

        SynapseResponse first = hub.sendPrompt("same prompt", null);
        SynapseResponse second = hub.sendPrompt("same prompt", null);

        assertThat(first.getContent()).isEqualTo("Cached answer");
        assertThat(second.getContent()).isEqualTo("Cached answer");
        assertThat(second).isSameAs(first);
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void differentPromptsAreNotShared() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"c1","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"A"},"finish_reason":"stop"}],"usage":{}}
                        """)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"c2","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"B"},"finish_reason":"stop"}],"usage":{}}
                        """)
                .addHeader("Content-Type", "application/json"));

        hub.sendPrompt("prompt one", null);
        hub.sendPrompt("prompt two", null);

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void cacheEvictionForcesFreshCall() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"c1","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"first"},"finish_reason":"stop"}],"usage":{}}
                        """)
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {"id":"c2","object":"chat.completion","model":"gpt-4","choices":[{"index":0,"message":{"role":"assistant","content":"second"},"finish_reason":"stop"}],"usage":{}}
                        """)
                .addHeader("Content-Type", "application/json"));

        hub.sendPrompt("k", null);
        cache.evict("gpt-4|k");
        hub.sendPrompt("k", null);

        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    static final class MapResponseCache implements ResponseCache {
        private final Map<String, SynapseResponse> map = new HashMap<>();

        @Override public Optional<SynapseResponse> get(String key) {
            return Optional.ofNullable(map.get(key));
        }
        @Override public void put(String key, SynapseResponse response) {
            map.put(key, response);
        }
        @Override public void evict(String key) {
            map.remove(key);
        }
        @Override public void clear() {
            map.clear();
        }
        int size() {
            return map.size();
        }
    }
}
