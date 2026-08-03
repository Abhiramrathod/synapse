package org.abhi.synapse.spring;

import org.abhi.synapse.config.SynapseConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "synapse")
public class SynapseProperties {

    private String baseUrl;
    private String endpoint;
    private String apiKey;
    private String modelName;
    private String provider;
    private double temperature;
    private int maxTokens;
    private Duration connectTimeout;
    private Duration readTimeout;
    private Duration timeout;
    private int maxRetries;
    private Duration retryDelay;
    private Duration requestTimeout;
    private Duration streamIdleTimeout;
    private boolean enableLogging;
    private int maxConcurrentRequests = 64;
    private int maxRequestsPerMinute = 0;
    private Duration circuitBreakerOpenDuration = Duration.ofSeconds(30);
    private int circuitBreakerFailureThreshold = 5;
    private Duration maxRetryElapsedTime = Duration.ofSeconds(120);

    public SynapseConfig toSynapseConfig() {
        SynapseConfig.Builder builder = SynapseConfig.builder()
                .baseUrl(baseUrl)
                .endpoint(endpoint)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .maxRetries(maxRetries)
                .retryDelay(retryDelay)
                .requestTimeout(requestTimeout)
                .enableLogging(enableLogging)
                .maxConcurrentRequests(maxConcurrentRequests)
                .maxRequestsPerMinute(maxRequestsPerMinute)
                .circuitBreakerOpenDuration(circuitBreakerOpenDuration)
                .circuitBreakerFailureThreshold(circuitBreakerFailureThreshold)
                .maxRetryElapsedTime(maxRetryElapsedTime);
        if (provider != null) builder.provider(provider);
        if (connectTimeout != null) builder.connectTimeout(connectTimeout);
        if (readTimeout != null) builder.readTimeout(readTimeout);
        if (timeout != null) builder.timeout(timeout);
        if (streamIdleTimeout != null) builder.streamIdleTimeout(streamIdleTimeout);
        return builder.build();
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public Duration getRetryDelay() { return retryDelay; }
    public void setRetryDelay(Duration retryDelay) { this.retryDelay = retryDelay; }
    public Duration getRequestTimeout() { return requestTimeout; }
    public void setRequestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; }
    public Duration getStreamIdleTimeout() { return streamIdleTimeout; }
    public void setStreamIdleTimeout(Duration streamIdleTimeout) { this.streamIdleTimeout = streamIdleTimeout; }
    public boolean isEnableLogging() { return enableLogging; }
    public void setEnableLogging(boolean enableLogging) { this.enableLogging = enableLogging; }
    public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
    public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }
    public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) { this.maxRequestsPerMinute = maxRequestsPerMinute; }
    public Duration getCircuitBreakerOpenDuration() { return circuitBreakerOpenDuration; }
    public void setCircuitBreakerOpenDuration(Duration circuitBreakerOpenDuration) { this.circuitBreakerOpenDuration = circuitBreakerOpenDuration; }
    public int getCircuitBreakerFailureThreshold() { return circuitBreakerFailureThreshold; }
    public void setCircuitBreakerFailureThreshold(int circuitBreakerFailureThreshold) { this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold; }
    public Duration getMaxRetryElapsedTime() { return maxRetryElapsedTime; }
    public void setMaxRetryElapsedTime(Duration maxRetryElapsedTime) { this.maxRetryElapsedTime = maxRetryElapsedTime; }
}
