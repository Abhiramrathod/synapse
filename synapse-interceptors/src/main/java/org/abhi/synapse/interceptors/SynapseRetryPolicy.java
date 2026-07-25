package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;

/**
 * Defines the retry strategy applied when a Synapse HTTP request or response fails.
 *
 * <p>Implementations of this interface control whether a failed operation should be retried,
 * how long to wait between retries, and the maximum number of retry attempts allowed. The
 * default implementation provides an exponential-backoff strategy with a base delay of 500ms
 * and a maximum of 3 retries.</p>
 *
 * <p>Implement this interface when you need custom retry semantics such as
 * retry-on-specific-status-codes, jittered backoff, or circuit-breaker-aware retry logic.</p>
 *
 * <p>Implementation example:</p>
 * <pre>{@code
 * public class RateLimitAwareRetryPolicy implements SynapseRetryPolicy {
 *
 *     @Override
 *     public boolean shouldRetry(int attempt, SynapseException error) {
 *         if (error instanceof RateLimitException && attempt < 5) {
 *             return true;
 *         }
 *         return attempt < getMaxRetries();
 *     }
 *
 *     @Override
 *     public long getDelay(int attempt) {
 *         // Jittered exponential backoff capped at 30 seconds
 *         long baseDelay = 1000L * (long) Math.pow(2, attempt);
 *         long jitter = ThreadLocalRandom.current().nextLong(0, baseDelay / 2);
 *         return Math.min(baseDelay + jitter, 30_000L);
 *     }
 *
 *     @Override
 *     public int getMaxRetries() {
 *         return 5;
 *     }
 * }
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseException
 */
public interface SynapseRetryPolicy {

    /**
     * Determines whether a failed request should be retried.
     *
     * <p>Called after each failed attempt. Return {@code true} to schedule another
     * retry or {@code false} to stop retrying and propagate the error. The default
     * implementation retries as long as the current attempt count is less than 3.</p>
     *
     * @param attempt the zero-based index of the current attempt (0 for the first retry)
     * @param error   the exception that caused the current attempt to fail
     * @return {@code true} if the request should be retried, {@code false} otherwise
     * @since 1.0.0
     */
    default boolean shouldRetry(int attempt, SynapseException error) {
        return attempt < 3;
    }

    /**
     * Calculates the delay in milliseconds before the next retry attempt.
     *
     * <p>Called after {@link #shouldRetry(int, SynapseException)} returns {@code true}.
     * The default implementation uses exponential backoff: {@code 500ms * 2^attempt},
     * producing delays of 500ms, 1000ms, 2000ms, and so on.</p>
     *
     * @param attempt the zero-based index of the upcoming retry attempt
     * @return the delay in milliseconds to wait before the next retry
     * @since 1.0.0
     */
    default long getDelay(int attempt) {
        return 500L * (long) Math.pow(2, attempt);
    }

    /**
     * Returns the maximum number of retry attempts allowed.
     *
     * <p>The default implementation returns 3, meaning the original request plus up to
     * three retry attempts for a total of four attempts.</p>
     *
     * @return the maximum number of retry attempts
     * @since 1.0.0
     */
    default int getMaxRetries() {
        return 3;
    }
}
