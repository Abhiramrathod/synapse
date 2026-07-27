package org.abhi.synapse.metrics;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseMetricsSummary;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

public class SynapseOpenTelemetryExporter implements SynapseMetricsListener {
    private final Object tracer;
    public SynapseOpenTelemetryExporter(Object tracer) { this.tracer = tracer; }
    @Override public void onRequestCompleted(SynapseMetricsSummary summary) {
        try {
            Object sb = tracer.getClass().getMethod("spanBuilder", String.class)
                    .invoke(tracer, "synapse.llm.request");
            sb = sb.getClass().getMethod("setAttribute", String.class, String.class)
                    .invoke(sb, "llm.model", summary.getModel() != null ? summary.getModel() : "unknown");
            sb = sb.getClass().getMethod("setAttribute", String.class, long.class)
                    .invoke(sb, "llm.latency.ms", summary.getLatencyMs());
            sb = sb.getClass().getMethod("setAttribute", String.class, long.class)
                    .invoke(sb, "llm.tokens.prompt", (long) summary.getPromptTokens());
            sb = sb.getClass().getMethod("setAttribute", String.class, long.class)
                    .invoke(sb, "llm.tokens.completion", (long) summary.getCompletionTokens());
            if (summary.getProvider() != null) sb = sb.getClass().getMethod("setAttribute", String.class, String.class)
                    .invoke(sb, "llm.provider", summary.getProvider());
            Object span = sb.getClass().getMethod("startSpan").invoke(sb);
            span.getClass().getMethod("end").invoke(span);
        } catch (Exception ignored) { }
    }
    @Override public void onRequestFailed(SynapseMetricsSummary summary, SynapseException error) {
        try {
            Object sb = tracer.getClass().getMethod("spanBuilder", String.class)
                    .invoke(tracer, "synapse.llm.request");
            sb = sb.getClass().getMethod("setAttribute", String.class, String.class)
                    .invoke(sb, "llm.model", summary.getModel());
            if (error != null) sb = sb.getClass().getMethod("setAttribute", String.class, String.class)
                    .invoke(sb, "error.type", error.getType().name());
            Object span = sb.getClass().getMethod("startSpan").invoke(sb);
            span.getClass().getMethod("end").invoke(span);
        } catch (Exception ignored) { }
    }
}
