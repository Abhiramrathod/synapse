package org.abhi.synapse.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** In-memory {@link RedisClient} for tests. */
final class InMemoryRedisClient implements RedisClient {

    final Map<String, byte[]> store = new HashMap<>();
    final AtomicInteger closeCount = new AtomicInteger();

    @Override
    public synchronized Optional<byte[]> get(byte[] key) {
        return Optional.ofNullable(store.get(new String(key, java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Override
    public synchronized void set(byte[] key, byte[] value, Duration ttl) {
        store.put(new String(key, java.nio.charset.StandardCharsets.UTF_8), value);
    }

    @Override
    public synchronized void delete(byte[] key) {
        store.remove(new String(key, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public void close() {
        closeCount.incrementAndGet();
    }
}
