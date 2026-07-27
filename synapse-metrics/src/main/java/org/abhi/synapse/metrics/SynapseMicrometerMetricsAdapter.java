package org.abhi.synapse.metrics;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseMetricsSummary;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

public class SynapseMicrometerMetricsAdapter implements SynapseMetricsListener {
    private final Object meterRegistry;
    public SynapseMicrometerMetricsAdapter(Object meterRegistry) { this.meterRegistry = meterRegistry; }
    @Override public void onRequestCompleted(SynapseMetricsSummary summary) {
        try {
            Class<?> registryClass = Class.forName("io.micrometer.core.instrument.MeterRegistry");
            Class<?> timerClass = Class.forName("io.micrometer.core.instrument.Timer");
            Object builder = timerClass.getMethod("builder", String.class).invoke(null, "synapse.request.duration");
            builder = builder.getClass().getMethod("tag", String.class, String.class)
                    .invoke(builder, "model", summary.getModel() != null ? summary.getModel() : "unknown");
            builder = builder.getClass().getMethod("tag", String.class, String.class)
                    .invoke(builder, "status", "success");
            if (summary.getProvider() != null) builder = builder.getClass().getMethod("tag", String.class, String.class)
                    .invoke(builder, "provider", summary.getProvider());
            Object timer = builder.getClass().getMethod("register", registryClass).invoke(builder, meterRegistry);
            timerClass.getMethod("record", long.class, java.util.concurrent.TimeUnit.class)
                    .invoke(timer, summary.getLatencyMs(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception ignored) { }
    }
    @Override public void onRequestFailed(SynapseMetricsSummary summary, SynapseException error) {
        try {
            Class<?> registryClass = Class.forName("io.micrometer.core.instrument.MeterRegistry");
            Class<?> counterClass = Class.forName("io.micrometer.core.instrument.Counter");
            Object builder = counterClass.getMethod("builder", String.class).invoke(null, "synapse.request.failures");
            builder = builder.getClass().getMethod("tag", String.class, String.class)
                    .invoke(builder, "model", summary.getModel() != null ? summary.getModel() : "unknown");
            if (error != null) builder = builder.getClass().getMethod("tag", String.class, String.class)
                    .invoke(builder, "error", error.getType().name());
            builder.getClass().getMethod("register", registryClass).invoke(builder, meterRegistry);
        } catch (Exception ignored) { }
    }
}
