package org.abhi.synapse.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * Minimal byte-oriented Redis client contract used by {@link RedisResponseCache}.
 *
 * <p>The library never depends on a concrete Redis driver. Implementations are
 * supplied at runtime, either directly or through a registered
 * {@link RedisClientProvider} service discovered via
 * {@link java.util.ServiceLoader}.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public interface RedisClient {

    /**
     * Reads the value stored at a key.
     *
     * @param key the key to read
     * @return the stored bytes, or {@link Optional#empty()} if the key is absent
     */
    Optional<byte[]> get(byte[] key);

    /**
     * Stores a value at a key with a time-to-live.
     *
     * @param key   the key to write
     * @param value the value to store
     * @param ttl   the expiration; must not be {@code null}
     */
    void set(byte[] key, byte[] value, Duration ttl);

    /**
     * Removes a key from the store.
     *
     * @param key the key to delete
     */
    void delete(byte[] key);

    /**
     * Releases any resources held by this client.
     *
     * @throws Exception if closing fails
     */
    void close() throws Exception;
}
