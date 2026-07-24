package org.abhi.synapse.interceptors;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseMetricsSummary;

public interface SynapseMetricsListener {
    default void onRequestStarted(String model) {}
    default void onRequestCompleted(SynapseMetricsSummary summary) {}
    default void onRequestFailed(SynapseMetricsSummary summary, SynapseException error) {}
}
