package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Supplier;

/**
 * Handles automatic retry logic for failed HTTP requests using exponential backoff.
 *
 * <p>This class wraps the execution of a request action and retries it when the action
 * throws a retryable {@link SynapseException}. The retry behavior is configurable via
 * either a {@link SynapseRetryPolicy} (if set in the configuration) or the fallback
 * settings from {@link SynapseConfig} (max retries and base delay).</p>
 *
 * <p>When a custom {@link SynapseRetryPolicy} is provided, it controls the maximum
 * number of retries, whether a given exception should be retried, and the delay
 * between attempts. Otherwise, exponential backoff is computed as
 * {@code baseDelay * 2^attempt}.</p>
 *
 * <p>This is an internal class within the {@code synapse-http} module and is not
 * intended for direct use by library consumers.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseConfig#getRetryPolicy()
 * @see SynapseRetryPolicy
 */
class SynapseRetryHandler {

    private static final Logger LOGGER = Logger.getLogger(SynapseRetryHandler.class.getName());

    private final SynapseConfig config;

    /**
     * Constructs a new {@code SynapseRetryHandler} with the specified configuration.
     *
     * @param config the {@link SynapseConfig} containing retry settings;
     *               must not be {@code null}
     * @since 1.0.0
     */
    SynapseRetryHandler(SynapseConfig config) {
        this.config = config;
    }

    /**
     * Executes the given action with automatic retry on failure.
     *
     * <p>The action is attempted up to {@code maxRetries + 1} times (one initial
     * attempt plus retries). If the action throws a retryable {@link SynapseException}
     * and retries remain, the handler sleeps with exponential backoff before retrying.
     * Non-retryable exceptions and exhausted retries are thrown immediately.</p>
     *
     * @param <T>    the return type of the action
     * @param action the {@link Supplier} representing the action to execute;
     *               must not be {@code null}
     * @return the result of the action on successful execution
     * @throws SynapseException if the action fails with a non-retryable exception,
     *                          if all retry attempts are exhausted, or if the
     *                          {@link SynapseRetryPolicy} denies a retry
     * @since 1.0.0
     */
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

    /**
     * Sleeps for a calculated delay before the next retry attempt.
     *
     * <p>If a custom {@link SynapseRetryPolicy} is provided, the delay is determined
     * by {@link SynapseRetryPolicy#getDelay(int)}. Otherwise, exponential backoff
     * is computed as {@code baseDelay * 2^attempt} milliseconds.</p>
     *
     * @param attempt the zero-based current attempt number
     * @param policy  the optional {@link SynapseRetryPolicy}; may be {@code null}
     * @since 1.0.0
     */
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
