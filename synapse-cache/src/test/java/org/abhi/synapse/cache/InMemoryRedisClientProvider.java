package org.abhi.synapse.cache;

/** SPI provider that supplies an {@link InMemoryRedisClient} for tests. */
public class InMemoryRedisClientProvider implements RedisClientProvider {

    static final InMemoryRedisClient SHARED = new InMemoryRedisClient();

    @Override
    public String name() {
        return "in-memory-test";
    }

    @Override
    public RedisClient create() {
        return SHARED;
    }
}
