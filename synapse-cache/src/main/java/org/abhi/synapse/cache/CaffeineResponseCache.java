package org.abhi.synapse.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.abhi.synapse.core.cache.ResponseCache;
import org.abhi.synapse.core.model.SynapseResponse;

import java.time.Duration;
import java.util.Optional;

/**
 * In-memory {@link ResponseCache} backed by Caffeine.
 *
 * <pre>{@code
 * ResponseCache cache = CaffeineResponseCache.builder()
 *         .maximumSize(10_000)
 *         .expireAfterWrite(Duration.ofMinutes(5))
 *         .build();
 * }</pre>
 *
 * <p>Thread-safe and designed for high read throughput. Entries are evicted
 * by size and/or write age as configured.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public final class CaffeineResponseCache implements ResponseCache {

    private final Cache<String, SynapseResponse> cache;

    CaffeineResponseCache(Cache<String, SynapseResponse> cache) {
        this.cache = cache;
    }

    /**
     * Creates a cache bounded by the given maximum number of entries.
     *
     * @param maximumSize the maximum number of entries
     * @return a size-bounded cache
     */
    public static CaffeineResponseCache ofMaximumSize(long maximumSize) {
        return builder().maximumSize(maximumSize).build();
    }

    /**
     * Creates a cache whose entries expire after the given duration.
     *
     * @param expireAfterWrite the time-to-live for entries
     * @return a time-bounded cache
     */
    public static CaffeineResponseCache expiringAfterWrite(Duration expireAfterWrite) {
        return builder().expireAfterWrite(expireAfterWrite).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Optional<SynapseResponse> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void put(String key, SynapseResponse response) {
        cache.put(key, response);
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    /**
     * Performs any pending maintenance work, including size-based eviction,
     * synchronously on the calling thread.
     */
    public void cleanUp() {
        cache.cleanUp();
    }

    /** Builder for {@link CaffeineResponseCache}. */
    public static final class Builder {
        private long maximumSize = Long.MAX_VALUE;
        private Duration expireAfterWrite;

        private Builder() {
        }

        public Builder maximumSize(long maximumSize) {
            if (maximumSize <= 0) {
                throw new IllegalArgumentException("maximumSize must be positive");
            }
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder expireAfterWrite(Duration expireAfterWrite) {
            if (expireAfterWrite == null || expireAfterWrite.isNegative() || expireAfterWrite.isZero()) {
                throw new IllegalArgumentException("expireAfterWrite must be a positive duration");
            }
            this.expireAfterWrite = expireAfterWrite;
            return this;
        }

        public CaffeineResponseCache build() {
            Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
            if (maximumSize != Long.MAX_VALUE) {
                caffeine.maximumSize(maximumSize);
            }
            if (expireAfterWrite != null) {
                caffeine.expireAfterWrite(expireAfterWrite);
            }
            return new CaffeineResponseCache(caffeine.build());
        }
    }
}
