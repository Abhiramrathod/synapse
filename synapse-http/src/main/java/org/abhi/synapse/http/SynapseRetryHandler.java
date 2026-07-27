package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

class SynapseRetryHandler {

    private static final Logger log = LoggerFactory.getLogger(SynapseRetryHandler.class);

    private final SynapseConfig config;

    SynapseRetryHandler(SynapseConfig config) {
        this.config = config;
    }

    <T> T executeWithRetry(Supplier<T> action) throws SynapseException {
        return executeWithRetry(action, null);
    }

    <T> T executeWithRetry(Supplier<T> action, Map<String, List<String>> lastHeaders) throws SynapseException {
        SynapseException lastException = null;
        SynapseRetryPolicy policy = config.getRetryPolicy();
        int maxRetries = policy != null ? policy.getMaxRetries() : config.getMaxRetries();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return action.get();
            } catch (SynapseException e) {
                lastException = e;
                if (!e.isRetryable() || attempt == maxRetries) {
                    if (e.isRetryable() && attempt == maxRetries && attempt > 0) {
                        throw new SynapseException("Max retries exhausted",
                                lastException, SynapseException.ExceptionType.RETRY_EXHAUSTED);
                    }
                    throw e;
                }
                if (policy != null && !policy.shouldRetry(attempt, e)) {
                    throw e;
                }
                log.warn("[Synapse] Request failed (attempt {}/{}), retrying...: {}",
                        attempt + 1, maxRetries + 1, e.getMessage());
                sleepBeforeRetry(attempt, policy, e, lastHeaders);
            }
        }

        throw new SynapseException("Max retries exhausted",
                lastException, SynapseException.ExceptionType.RETRY_EXHAUSTED);
    }

    private void sleepBeforeRetry(int attempt, SynapseRetryPolicy policy,
                                   SynapseException exception, Map<String, List<String>> headers) {
        try {
            Map<String, List<String>> hdrs = headers != null ? headers : Collections.emptyMap();
            long delay = policy != null
                    ? policy.getDelay(attempt, exception, hdrs)
                    : config.getRetryDelay().toMillis() * (long) Math.pow(2, attempt);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
