package org.abhi.synapse.metrics;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseMetricsSummary;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SynapseMetricsCollector {

    private static final Logger LOGGER = Logger.getLogger(SynapseMetricsCollector.class.getName());

    private final SynapseMetrics metrics;
    private final SynapseConfig config;

    public SynapseMetricsCollector(SynapseMetrics metrics, SynapseConfig config) {
        this.metrics = metrics;
        this.config = config;
    }

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

    public void recordSuccess(long startTime) {
        recordSuccess(startTime, 0, 0);
    }

    public void recordFailure(long startTime) {
        long latencyMs = System.currentTimeMillis() - startTime;
        metrics.recordRequest(config.getModelName(), latencyMs, 0, 0, false);
        SynapseMetricsListener listener = config.getMetricsListener();
        if (listener != null) {
            listener.onRequestFailed(new SynapseMetricsSummary(
                    config.getModelName(), latencyMs, 0, 0, false), null);
        }
    }

    public SynapseException recordFailureAndThrow(long startTime, String message,
                                                    Throwable cause, SynapseException.ExceptionType type) {
        recordFailure(startTime);
        return new SynapseException(message, cause, type);
    }
}
