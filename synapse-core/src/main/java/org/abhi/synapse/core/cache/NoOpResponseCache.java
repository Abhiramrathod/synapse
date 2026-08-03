package org.abhi.synapse.core.cache;

import org.abhi.synapse.core.model.SynapseResponse;

import java.util.Optional;

/**
 * A {@link ResponseCache} that stores nothing and always reports a miss.
 *
 * <p>Used as the default when no cache is configured, so callers can treat
 * caching as always-optional.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public final class NoOpResponseCache implements ResponseCache {

    private static final NoOpResponseCache INSTANCE = new NoOpResponseCache();

    private NoOpResponseCache() {
    }

    public static NoOpResponseCache instance() {
        return INSTANCE;
    }

    @Override
    public Optional<SynapseResponse> get(String key) {
        return Optional.empty();
    }

    @Override
    public void put(String key, SynapseResponse response) {
        // no-op
    }

    @Override
    public void evict(String key) {
        // no-op
    }

    @Override
    public void clear() {
        // no-op
    }
}
