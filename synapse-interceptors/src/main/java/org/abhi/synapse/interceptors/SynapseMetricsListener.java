package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseMetricsSummary;

/**
 * Listener for collecting metrics and telemetry data across the Synapse request lifecycle.
 *
 * <p>Implementations of this interface receive callbacks at key points in the request lifecycle,
 * enabling integration with monitoring systems such as Micrometer, Prometheus, Datadog, or
 * custom metrics stores. Listeners can track request counts, latency histograms, error rates,
 * and other operational metrics.</p>
 *
 * <p>All methods have default (no-op) implementations so that implementors may override only
 * the callbacks they need. Multiple listeners can be registered simultaneously and will be
 * invoked in registration order.</p>
 *
 * <p>Implementation example:</p>
 * <pre>{@code
 * public class MicrometerMetricsListener implements SynapseMetricsListener {
 *
 *     private final MeterRegistry registry;
 *     private final ThreadLocal<Long> startTime = new ThreadLocal<>();
 *
 *     public MicrometerMetricsListener(MeterRegistry registry) {
 *         this.registry = registry;
 *     }
 *
 *     @Override
 *     public void onRequestStarted(String model) {
 *         startTime.set(System.nanoTime());
 *         registry.counter("synapse.requests.started", "model", model).increment();
 *     }
 *
 *     @Override
 *     public void onRequestCompleted(SynapseMetricsSummary summary) {
 *         long elapsed = System.nanoTime() - startTime.get();
 *         Timer.builder("synapse.request.duration")
 *              .tag("model", summary.getModel())
 *              .tag("status", "success")
 *              .register(registry)
 *              .record(elapsed, TimeUnit.NANOSECONDS);
 *     }
 *
 *     @Override
 *     public void onRequestFailed(SynapseMetricsSummary summary, SynapseException error) {
 *         long elapsed = System.nanoTime() - startTime.get();
 *         Timer.builder("synapse.request.duration")
 *              .tag("model", summary.getModel())
 *              .tag("status", "error")
 *              .register(registry)
 *              .record(elapsed, TimeUnit.NANOSECONDS);
 *         registry.counter("synapse.requests.failed",
 *                  "model", summary.getModel(),
 *                  "error", error.getClass().getSimpleName()).increment();
 *     }
 * }
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseMetricsSummary
 * @see SynapseRequestInterceptor
 * @see SynapseResponseInterceptor
 */
public interface SynapseMetricsListener {

    /**
     * Called when a new request is about to be initiated.
     *
     * <p>Use this callback to record the start of a request, initialize timing
     * state, or increment in-flight request counters. The {@code model} parameter
     * identifies the target model or service endpoint being called.</p>
     *
     * @param model the identifier of the target model or service being invoked
     * @since 1.0.0
     */
    default void onRequestStarted(String model) {}

    /**
     * Called after a request has been completed successfully.
     *
     * <p>Use this callback to record latency, payload size, token usage, and other
     * success-oriented metrics from the provided {@link SynapseMetricsSummary}.</p>
     *
     * @param summary a snapshot of metrics captured during the completed request,
     *                including timing, token counts, and model information
     * @since 1.0.0
     */
    default void onRequestCompleted(SynapseMetricsSummary summary) {}

    /**
     * Called when a request fails with an error.
     *
     * <p>Use this callback to record failure counts, error-type breakdowns, and
     * failed-request latency. The {@link SynapseMetricsSummary} contains any metrics
     * that were captured before the failure occurred.</p>
     *
     * @param summary a snapshot of metrics captured up to the point of failure
     * @param error   the exception that caused the request to fail
     * @since 1.0.0
     */
    default void onRequestFailed(SynapseMetricsSummary summary, SynapseException error) {}
}
