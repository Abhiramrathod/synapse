package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Supplier;

class SynapseRetryHandler {

    private static final Logger LOGGER = Logger.getLogger(SynapseRetryHandler.class.getName());

    private final SynapseConfig config;

    SynapseRetryHandler(SynapseConfig config) {
        this.config = config;
    }

    <T> T executeWithRetry(Supplier<T> action) throws SynapseException {
        SynapseException lastException = null;
        SynapseRetryPolicy policy = config.getRetryPolicy();
        int maxRetries = policy != null ? policy.getMaxRetries() : config.getMaxRetries();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return action.get();
            } catch (SynapseException e) {
                lastException = e;
                if (!e.isRetryable() || attempt == maxRetries) {
                    throw e;
                }
                if (policy != null && !policy.shouldRetry(attempt, e)) {
                    throw e;
                }
                if (config.isEnableLogging()) {
                    LOGGER.log(Level.WARNING, "[Synapse] Request failed (attempt "
                            + (attempt + 1) + "/" + (maxRetries + 1)
                            + "), retrying...: " + e.getMessage());
                }
                sleepBeforeRetry(attempt, policy);
            }
        }

        throw new SynapseException("Max retries exhausted",
                lastException, SynapseException.ExceptionType.RETRY_EXHAUSTED);
    }

    private void sleepBeforeRetry(int attempt, SynapseRetryPolicy policy) {
        try {
            long delay = policy != null ? policy.getDelay(attempt)
                    : config.getRetryDelay().toMillis() * (long) Math.pow(2, attempt);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
