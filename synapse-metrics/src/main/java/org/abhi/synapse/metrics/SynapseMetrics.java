package org.abhi.synapse.metrics;


import java.util.ArrayList;
import java.util.List;

public class SynapseMetrics {

    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private long totalPromptTokens;
    private long totalCompletionTokens;
    private long totalLatencyMs;
    private final List<RequestMetric> requestMetrics = new ArrayList<>();

    public void recordRequest(String model, long latencyMs, int promptTokens,
                              int completionTokens, boolean success) {
        totalRequests++;
        if (success) {
            successfulRequests++;
        } else {
            failedRequests++;
        }
        totalPromptTokens += promptTokens;
        totalCompletionTokens += completionTokens;
        totalLatencyMs += latencyMs;
        requestMetrics.add(new RequestMetric(model, latencyMs, promptTokens,
                completionTokens, success));
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public long getTotalPromptTokens() {
        return totalPromptTokens;
    }

    public long getTotalCompletionTokens() {
        return totalCompletionTokens;
    }

    public long getTotalTokens() {
        return totalPromptTokens + totalCompletionTokens;
    }

    public long getTotalLatencyMs() {
        return totalLatencyMs;
    }

    public double getAverageLatencyMs() {
        if (totalRequests == 0) return 0;
        return (double) totalLatencyMs / totalRequests;
    }

    public double getSuccessRate() {
        if (totalRequests == 0) return 0;
        return (double) successfulRequests / totalRequests * 100;
    }

    public List<RequestMetric> getRequestMetrics() {
        return List.copyOf(requestMetrics);
    }

    public void reset() {
        totalRequests = 0;
        successfulRequests = 0;
        failedRequests = 0;
        totalPromptTokens = 0;
        totalCompletionTokens = 0;
        totalLatencyMs = 0;
        requestMetrics.clear();
    }

    public static class RequestMetric {
        private final String model;
        private final long latencyMs;
        private final int promptTokens;
        private final int completionTokens;
        private final boolean success;

        public RequestMetric(String model, long latencyMs, int promptTokens,
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

        public boolean isSuccess() {
            return success;
        }
    }
}
