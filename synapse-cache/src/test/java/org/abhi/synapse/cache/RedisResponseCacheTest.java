package org.abhi.synapse.cache;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisResponseCacheTest {

    @Test
    void putThenGetRoundTripsResponse() {
        InMemoryRedisClient client = new InMemoryRedisClient();
        RedisResponseCache cache = new RedisResponseCache(client);
        SynapseResponse response = response("from redis", 42, 7);

        cache.put("prompt", response);

        Optional<SynapseResponse> hit = cache.get("prompt");
        assertThat(hit).isPresent();
        assertThat(hit.get().getContent()).isEqualTo("from redis");
        assertThat(hit.get().getPromptTokens()).isEqualTo(42);
        assertThat(hit.get().getCompletionTokens()).isEqualTo(7);
    }

    @Test
    void missingKeyReturnsEmpty() {
        RedisResponseCache cache = new RedisResponseCache(new InMemoryRedisClient());

        assertThat(cache.get("nope")).isEmpty();
    }

    @Test
    void evictDeletesTheKey() {
        InMemoryRedisClient client = new InMemoryRedisClient();
        RedisResponseCache cache = new RedisResponseCache(client);
        cache.put("k", response("v"));

        cache.evict("k");

        assertThat(cache.get("k")).isEmpty();
        assertThat(client.store).isEmpty();
    }

    @Test
    void keysAreWrittenWithPrefix() {
        InMemoryRedisClient client = new InMemoryRedisClient();
        RedisResponseCache cache = new RedisResponseCache(client, null, "synapse:", Duration.ofMinutes(1));

        cache.put("prompt", response("v"));

        assertThat(client.store.keySet()).containsExactly("synapse:prompt");
    }

    @Test
    void viaServiceLoaderFindsRegisteredProvider() throws SynapseException {
        InMemoryRedisClientProvider.SHARED.store.clear();

        RedisResponseCache cache = RedisResponseCache.viaServiceLoader();

        cache.put("k", response("spi!"));
        assertThat(cache.get("k")).get().extracting(SynapseResponse::getContent).isEqualTo("spi!");
    }

    @Test
    void clearIsUnsupported() {
        RedisResponseCache cache = new RedisResponseCache(new InMemoryRedisClient());

        assertThatThrownBy(cache::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void closeClosesTheClient() throws Exception {
        InMemoryRedisClient client = new InMemoryRedisClient();
        RedisResponseCache cache = new RedisResponseCache(client);

        cache.close();

        assertThat(client.closeCount.get()).isEqualTo(1);
    }

    @Test
    void constructorRejectsNullClient() {
        assertThatThrownBy(() -> new RedisResponseCache(null)).isInstanceOf(IllegalArgumentException.class);
    }

    private static SynapseResponse response(String content) {
        return response(content, 0, 0);
    }

    private static SynapseResponse response(String content, int promptTokens, int completionTokens) {
        SynapseResponse response = new SynapseResponse();
        response.setContent(content);
        response.setPromptTokens(promptTokens);
        response.setCompletionTokens(completionTokens);
        return response;
    }
}
