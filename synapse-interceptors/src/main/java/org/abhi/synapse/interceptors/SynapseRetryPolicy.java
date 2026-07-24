package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;

public interface SynapseRetryPolicy {
    default boolean shouldRetry(int attempt, SynapseException error) {
        return attempt < 3;
    }

    default long getDelay(int attempt) {
        return 500L * (long) Math.pow(2, attempt);
    }

    default int getMaxRetries() {
        return 3;
    }
}
