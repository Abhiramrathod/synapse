package org.abhi.synapse.core.model;

public class SynapseMetricsSummary {

    private final String model;
    private final long latencyMs;
    private final int promptTokens;
    private final int completionTokens;
    private final boolean success;

    public SynapseMetricsSummary(String model, long latencyMs, int promptTokens,
                                  int completionTokens, boolean success) {
        this.model = model;
        this.latencyMs = latencyMs;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.success = success;
    }

    public String getModel() {
        return model;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    public boolean isSuccess() {
        return success;
    }
}
