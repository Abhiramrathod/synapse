package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ResponseFormat;
import org.abhi.synapse.core.model.ToolDefinition;
import java.time.Duration;
import java.util.List;

public class RequestOptions {
    private String modelName;
    private Double temperature;
    private Integer maxTokens;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Duration requestTimeout;
    private Duration streamIdleTimeout;
    private List<ToolDefinition> tools;
    private ResponseFormat responseFormat;

    public RequestOptions() {}
    public String getModelName() { return modelName; }
    public RequestOptions setModelName(String modelName) { this.modelName = modelName; return this; }
    public Double getTemperature() { return temperature; }
    public RequestOptions setTemperature(double temperature) { this.temperature = temperature; return this; }
    public Integer getMaxTokens() { return maxTokens; }
    public RequestOptions setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public RequestOptions setConnectTimeout(Duration ct) { this.connectTimeout = ct; return this; }
    public Duration getReadTimeout() { return readTimeout; }
    public RequestOptions setReadTimeout(Duration rt) { this.readTimeout = rt; return this; }
    public Duration getRequestTimeout() { return requestTimeout; }
    public RequestOptions setRequestTimeout(Duration rt) { this.requestTimeout = rt; return this; }
    public Duration getStreamIdleTimeout() { return streamIdleTimeout; }
    public RequestOptions setStreamIdleTimeout(Duration st) { this.streamIdleTimeout = st; return this; }
    public List<ToolDefinition> getTools() { return tools; }
    public RequestOptions setTools(List<ToolDefinition> tools) { this.tools = tools; return this; }
    public ResponseFormat getResponseFormat() { return responseFormat; }
    public RequestOptions setResponseFormat(ResponseFormat rf) { this.responseFormat = rf; return this; }
    public static RequestOptions defaults() { return new RequestOptions(); }
}
