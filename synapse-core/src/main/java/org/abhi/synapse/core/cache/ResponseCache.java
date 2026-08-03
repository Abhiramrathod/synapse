package org.abhi.synapse.core.cache;

import org.abhi.synapse.core.model.SynapseResponse;

import java.util.Optional;

/**
 * Pluggable cache for LLM responses, keyed by request.
 *
 * <p>Repeating static queries (system prompts, repeated classification tasks)
 * burn API credits unnecessarily. Attach a cache to a hub so that identical
 * prompts are served from the cache instead of the provider:</p>
 *
 * <pre>{@code
 * ResponseCache cache = CaffeineResponseCache.builder()
 *         .maximumSize(10_000)
 *         .expireAfterWrite(Duration.ofMinutes(5))
 *         .build();
 * SynapseConfig config = SynapseConfig.builder()
 *         .baseUrl("...").endpoint("/v1/chat/completions")
 *         .apiKey("...").modelName("gpt-4o")
 *         .cache(cache)
 *         .build();
 * }</pre>
 *
 * <p>Built-in adapters ship in the {@code synapse-cache} module:
 * {@code CaffeineResponseCache} (in-memory) and {@code RedisResponseCache}
 * (distributed, discovered via SPI). Implementations must be thread-safe.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public interface ResponseCache extends AutoCloseable {

    /**
     * Returns the cached response for a key, if present.
     *
     * @param key the cache key (typically prompt + model)
     * @return the cached response, or {@link Optional#empty()} on a miss
     */
    Optional<SynapseResponse> get(String key);

    /**
     * Stores a response under the given key.
     *
     * @param key      the cache key
     * @param response the response to cache
     */
    void put(String key, SynapseResponse response);

    /**
     * Removes a single entry from the cache.
     *
     * @param key the cache key to evict
     */
    void evict(String key);

    /**
     * Clears all entries from the cache.
     *
     * <p>Distributed backends that cannot enumerate keys may throw
     * {@link UnsupportedOperationException}.</p>
     */
    void clear();

    /**
     * Releases any resources held by this cache.
     *
     * @throws Exception if closing fails
     */
    @Override
    default void close() throws Exception {
    }
}
