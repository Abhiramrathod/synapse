package org.abhi.synapse.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynapseResponseContext {

    private int statusCode;
    private String body;
    private final Map<String, List<String>> headers;
    private final long latencyMs;
    private final String model;
    private int tokensUsed;

    public SynapseResponseContext(int statusCode, String body, Map<String, List<String>> headers,
                                   long latencyMs, String model, int tokensUsed) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>(headers);
        this.latencyMs = latencyMs;
        this.model = model;
        this.tokensUsed = tokensUsed;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, List<String>> getHeaders() {
        return Map.copyOf(headers);
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public String getModel() {
        return model;
    }

    public int getTokensUsed() {
        return tokensUsed;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isRetryable() {
        return statusCode == 429 || statusCode >= 500;
    }
}
