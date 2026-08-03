package org.abhi.synapse.cache;

/**
 * Service-provider interface for obtaining a {@link RedisClient}.
 *
 * <p>Implementations are registered via
 * {@code META-INF/services/org.abhi.synapse.cache.RedisClientProvider} and
 * discovered with {@link java.util.ServiceLoader}. This lets consumers plug in
 * their own Redis driver (Lettuce, Jedis, Redisson, ...) without the library
 * depending on any of them.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public interface RedisClientProvider {

    /**
     * A human-readable name for this provider (e.g. {@code "lettuce"}).
     *
     * @return the provider name; never {@code null}
     */
    String name();

    /**
     * Creates a new connected {@link RedisClient}.
     *
     * @return a ready-to-use client
     */
    RedisClient create();
}
