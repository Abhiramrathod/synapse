package org.abhi.synapse.metrics;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseMetricsSummary;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collects and records metrics for Synapse HTTP requests.
 *
 * <p>Acts as the bridge between request execution and metrics storage. After each
 * request completes (successfully or with failure), the caller invokes the appropriate
 * {@code record*} method which computes latency, updates the {@link SynapseMetrics}
 * aggregate counters, notifies any registered {@link SynapseMetricsListener}, and
 * optionally logs the result.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseMetrics
 * @see SynapseConfig
 * @see SynapseMetricsListener
 */
public class SynapseMetricsCollector {

    private static final Logger LOGGER = Logger.getLogger(SynapseMetricsCollector.class.getName());

    private final SynapseMetrics metrics;
    private final SynapseConfig config;

    /**
     * Constructs a new {@code SynapseMetricsCollector} with the specified metrics store and configuration.
     *
     * @param metrics the {@link SynapseMetrics} instance used to store recorded metrics
     * @param config  the {@link SynapseConfig} providing model name, logging preferences, and listener references
     * @since 1.0.0
     */
    public SynapseMetricsCollector(SynapseMetrics metrics, SynapseConfig config) {
        this.metrics = metrics;
        this.config = config;
    }

    /**
     * Records a successful request with full token usage details.
     *
     * <p>Computes latency from the given {@code startTime}, stores the metrics via
     * {@link SynapseMetrics#recordRequest}, notifies the registered
     * {@link SynapseMetricsListener} (if any), and logs a {@code FINE}-level message
     * when logging is enabled.</p>
     *
     * @param startTime       the epoch millis timestamp when the request was initiated
     * @param promptTokens    the number of prompt tokens consumed
     * @param completionTokens the number of completion tokens generated
     * @since 1.0.0
     */
    public void recordSuccess(long startTime, int promptTokens, int completionTokens) {
        long latencyMs = System.currentTimeMillis() - startTime;
        metrics.recordRequest(config.getModelName(), latencyMs,
                promptTokens, completionTokens, true);
        SynapseMetricsListener listener = config.getMetricsListener();
        if (listener != null) {
            listener.onRequestCompleted(new SynapseMetricsSummary(
                    config.getModelName(), latencyMs, promptTokens, completionTokens, true));
        }
        if (config.isEnableLogging()) {
            LOGGER.log(Level.FINE, "[Synapse] Response in " + latencyMs + "ms, tokens: "
                    + promptTokens + " prompt + " + completionTokens + " completion");
        }
    }

    /**
     * Records a successful request without token usage information.
     *
     * <p>Convenience overload equivalent to {@code recordSuccess(startTime, 0, 0)}.
     * Useful when token counts are unavailable from the response.</p>
     *
     * @param startTime the epoch millis timestamp when the request was initiated
     * @since 1.0.0
     */
    public void recordSuccess(long startTime) {
        recordSuccess(startTime, 0, 0);
    }

    /**
     * Records a failed request.
     *
     * <p>Computes latency from the given {@code startTime}, stores zero-token failure
     * metrics via {@link SynapseMetrics#recordRequest}, and notifies the registered
     * {@link SynapseMetricsListener} (if any) with a {@code null} cause.</p>
     *
     * @param startTime the epoch millis timestamp when the request was initiated
     * @since 1.0.0
     */
    public void recordFailure(long startTime) {
        long latencyMs = System.currentTimeMillis() - startTime;
        metrics.recordRequest(config.getModelName(), latencyMs, 0, 0, false);
        SynapseMetricsListener listener = config.getMetricsListener();
        if (listener != null) {
            listener.onRequestFailed(new SynapseMetricsSummary(
                    config.getModelName(), latencyMs, 0, 0, false), null);
        }
    }

    /**
     * Records a failed request and returns a {@link SynapseException} for the caller to throw.
     *
     * <p>This is a convenience method that combines {@link #recordFailure(long)} with the
     * construction of a {@link SynapseException}, enabling callers to use a single statement
     * to record and propagate errors.</p>
     *
     * @param startTime the epoch millis timestamp when the request was initiated
     * @param message   the error message describing the failure
     * @param cause     the underlying cause (may be {@code null})
     * @param type      the {@link SynapseException.ExceptionType} categorizing the failure
     * @return a new {@link SynapseException} ready to be thrown by the caller
     * @since 1.0.0
     */
    public SynapseException recordFailureAndThrow(long startTime, String message,
                                                    Throwable cause, SynapseException.ExceptionType type) {
        recordFailure(startTime);
        return new SynapseException(message, cause, type);
    }
}
