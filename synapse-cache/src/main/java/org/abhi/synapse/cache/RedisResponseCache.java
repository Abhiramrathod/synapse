package org.abhi.synapse.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.abhi.synapse.core.cache.ResponseCache;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Distributed {@link ResponseCache} backed by Redis.
 *
 * <p>Responses are serialized to JSON bytes and stored with a time-to-live.
 * The concrete Redis driver is supplied through {@link RedisClientProvider}
 * SPI so the library carries no Redis dependency:</p>
 *
 * <pre>{@code
 * // registered META-INF/services/...RedisClientProvider
 * ResponseCache cache = RedisResponseCache.viaServiceLoader();
 *
 * // or supply a client directly
 * ResponseCache cache = new RedisResponseCache(myRedisClient);
 * }</pre>
 *
 * <p>Because Redis cannot enumerate keys, {@link #clear()} is unsupported;
 * evict individual keys with {@link #evict(String)} instead.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public final class RedisResponseCache implements ResponseCache {

    private final RedisClient client;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;
    private final Duration ttl;

    public RedisResponseCache(RedisClient client) {
        this(client, new ObjectMapper(), "synapse:", Duration.ofMinutes(30));
    }

    public RedisResponseCache(RedisClient client, ObjectMapper objectMapper, String keyPrefix, Duration ttl) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        this.client = client;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.keyPrefix = keyPrefix != null ? keyPrefix : "";
        this.ttl = ttl;
    }

    /**
     * Creates a cache backed by the first {@link RedisClientProvider} found on
     * the classpath via {@link ServiceLoader}.
     *
     * @return a Redis-backed cache
     * @throws SynapseException if no provider is registered
     */
    public static RedisResponseCache viaServiceLoader() throws SynapseException {
        List<RedisClientProvider> providers = new ArrayList<>();
        ServiceLoader.load(RedisClientProvider.class).forEach(providers::add);
        if (providers.isEmpty()) {
            throw new SynapseException("No RedisClientProvider registered. Add META-INF/services/"
                    + "org.abhi.synapse.cache.RedisClientProvider to your application.",
                    SynapseException.ExceptionType.CONFIG_ERROR);
        }
        return new RedisResponseCache(providers.get(0).create());
    }

    @Override
    public Optional<SynapseResponse> get(String key) {
        return client.get(redisKey(key)).flatMap(this::deserialize);
    }

    @Override
    public void put(String key, SynapseResponse response) {
        client.set(redisKey(key), serialize(response), ttl);
    }

    @Override
    public void evict(String key) {
        client.delete(redisKey(key));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(
                "Redis cannot enumerate keys; evict individual keys with evict(String) instead");
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private byte[] redisKey(String key) {
        return (keyPrefix + key).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] serialize(SynapseResponse response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            throw new SynapseException("Failed to serialize response for Redis cache", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    private Optional<SynapseResponse> deserialize(byte[] bytes) {
        try {
            return Optional.ofNullable(objectMapper.readValue(bytes, SynapseResponse.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
