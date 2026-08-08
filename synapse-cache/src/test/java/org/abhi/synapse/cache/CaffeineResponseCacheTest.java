package org.abhi.synapse.cache;

import org.abhi.synapse.core.cache.ResponseCache;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaffeineResponseCacheTest {

    @Test
    void putThenGetReturnsStoredResponse() {
        CaffeineResponseCache cache = CaffeineResponseCache.builder().maximumSize(100).build();
        SynapseResponse response = response("cached!");

        cache.put("prompt", response);

        assertThat(cache.get("prompt")).isPresent().get()
                .extracting(SynapseResponse::getContent).isEqualTo("cached!");
    }

    @Test
    void missingKeyReturnsEmpty() {
        CaffeineResponseCache cache = CaffeineResponseCache.builder().maximumSize(100).build();

        assertThat(cache.get("nope")).isEqualTo(Optional.empty());
    }

    @Test
    void evictRemovesEntry() {
        CaffeineResponseCache cache = CaffeineResponseCache.builder().maximumSize(100).build();
        cache.put("key", response("v"));

        cache.evict("key");

        assertThat(cache.get("key")).isEmpty();
    }

    @Test
    void clearRemovesEveryEntry() {
        CaffeineResponseCache cache = CaffeineResponseCache.builder().maximumSize(100).build();
        cache.put("a", response("1"));
        cache.put("b", response("2"));

        cache.clear();

        assertThat(cache.get("a")).isEmpty();
        assertThat(cache.get("b")).isEmpty();
    }

    @Test
    void maximumSizeEvictsOldestEntries() {
        CaffeineResponseCache cache = CaffeineResponseCache.builder().maximumSize(2).build();
        cache.put("a", response("1"));
        cache.put("b", response("2"));
        cache.put("c", response("3"));
        cache.cleanUp();

        assertThat(cache.get("a")).isEmpty();
        assertThat(cache.get("b")).isPresent();
        assertThat(cache.get("c")).isPresent();
    }

    @Test
    void entriesExpireAfterWrite() throws Exception {
        CaffeineResponseCache cache = CaffeineResponseCache.builder().expireAfterWrite(Duration.ofMillis(50)).build();
        cache.put("key", response("v"));

        assertThat(cache.get("key")).isPresent();
        Thread.sleep(120);
        assertThat(cache.get("key")).isEmpty();
    }

    @Test
    void builderRejectsInvalidArguments() {
        assertThatThrownBy(() -> CaffeineResponseCache.builder().maximumSize(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CaffeineResponseCache.builder().expireAfterWrite(Duration.ofSeconds(0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CaffeineResponseCache.builder().expireAfterWrite(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cacheIsUsableThroughInterface() {
        ResponseCache cache = CaffeineResponseCache.builder().maximumSize(10).build();

        cache.put("k", response("hello"));
        assertThat(cache.get("k")).isPresent();
    }

    private static SynapseResponse response(String content) {
        SynapseResponse response = new SynapseResponse();
        response.setContent(content);
        return response;
    }
}
