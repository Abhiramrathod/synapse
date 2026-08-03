package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ResponseFormat;
import org.abhi.synapse.core.model.ToolDefinition;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class RequestOptions {
    private String modelName;
    private Double temperature;
    private Integer maxTokens;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Duration requestTimeout;
    private Duration streamIdleTimeout;
    private List<ToolDefinition> tools;
    private List<Object> toolInstances;
    private ResponseFormat responseFormat;
    private Map<String, Object> variables;

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
    public List<Object> getToolInstances() { return toolInstances; }
    public RequestOptions setToolInstances(List<Object> toolInstances) { this.toolInstances = toolInstances; return this; }
    public boolean hasTools() {
        return (tools != null && !tools.isEmpty()) || (toolInstances != null && !toolInstances.isEmpty());
    }
    public ResponseFormat getResponseFormat() { return responseFormat; }
    public RequestOptions setResponseFormat(ResponseFormat rf) { this.responseFormat = rf; return this; }
    public Map<String, Object> getVariables() { return variables; }
    public RequestOptions setVariables(Map<String, Object> variables) { this.variables = variables; return this; }
    public static RequestOptions defaults() { return new RequestOptions(); }
}
