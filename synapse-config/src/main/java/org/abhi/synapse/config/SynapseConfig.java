package org.abhi.synapse.config;

import org.abhi.synapse.core.AbstractSynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

import java.time.Duration;

public class SynapseConfig extends AbstractSynapseConfig {

    private final String baseUrl;
    private final String endpoint;
    private final String apiKey;
    private final String modelName;
    private final String provider;
    private final SynapseRequestInterceptor requestInterceptor;
    private final SynapseResponseInterceptor responseInterceptor;
    private final SynapseRetryPolicy retryPolicy;
    private final SynapseMetricsListener metricsListener;
    private final int maxConcurrentRequests;
    private final int maxRequestsPerMinute;
    private final Duration circuitBreakerOpenDuration;
    private final int circuitBreakerFailureThreshold;

    private SynapseConfig(Builder builder) {
        super(builder.temperature, builder.maxTokens, builder.connectTimeout,
              builder.readTimeout, builder.maxRetries, builder.retryDelay,
              builder.requestTimeout, builder.streamIdleTimeout, builder.enableLogging,
              builder.maxRetryElapsedTime);
        this.baseUrl = builder.baseUrl;
        this.endpoint = builder.endpoint;
        this.apiKey = builder.apiKey;
        this.modelName = builder.modelName;
        this.provider = builder.provider;
        this.requestInterceptor = builder.requestInterceptor;
        this.responseInterceptor = builder.responseInterceptor;
        this.retryPolicy = builder.retryPolicy;
        this.metricsListener = builder.metricsListener;
        this.maxConcurrentRequests = builder.maxConcurrentRequests;
        this.maxRequestsPerMinute = builder.maxRequestsPerMinute;
        this.circuitBreakerOpenDuration = builder.circuitBreakerOpenDuration;
        this.circuitBreakerFailureThreshold = builder.circuitBreakerFailureThreshold;
    }

    public void validate() throws SynapseException {
        if (baseUrl == null || baseUrl.isBlank()) throw new SynapseException("baseUrl is required");
        if (endpoint == null || endpoint.isBlank()) throw new SynapseException("endpoint is required");
        if (apiKey == null || apiKey.isBlank()) throw new SynapseException("apiKey is required");
        if (modelName == null || modelName.isBlank()) throw new SynapseException("modelName is required");
        if (provider == null || provider.isBlank()) throw new SynapseException("provider is required");
    }

    public String getBaseUrl() { return baseUrl; }
    public String getEndpoint() { return endpoint; }
    public String getApiKey() { return apiKey; }
    public String getModelName() { return modelName; }
    public String getProvider() { return provider; }
    public SynapseRequestInterceptor getRequestInterceptor() { return requestInterceptor; }
    public SynapseResponseInterceptor getResponseInterceptor() { return responseInterceptor; }
    public SynapseRetryPolicy getRetryPolicy() { return retryPolicy; }
    public SynapseMetricsListener getMetricsListener() { return metricsListener; }
    public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
    public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
    public Duration getCircuitBreakerOpenDuration() { return circuitBreakerOpenDuration; }
    public int getCircuitBreakerFailureThreshold() { return circuitBreakerFailureThreshold; }

    @Override
    public String toString() {
        return "SynapseConfig{baseUrl='" + baseUrl + "', endpoint='" + endpoint
                + "', apiKey='***REDACTED***', modelName='" + modelName + "'}";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String baseUrl;
        private String endpoint;
    private String apiKey;
    private String modelName;
    private String provider = "openai";
        private double temperature = DEFAULT_TEMPERATURE;
        private int maxTokens = DEFAULT_MAX_TOKENS;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration readTimeout = DEFAULT_READ_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private Duration retryDelay = DEFAULT_RETRY_DELAY;
        private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        private Duration streamIdleTimeout = DEFAULT_STREAM_IDLE_TIMEOUT;
        private boolean enableLogging = DEFAULT_ENABLE_LOGGING;
        private Duration maxRetryElapsedTime = Duration.ofSeconds(120);
        private SynapseRequestInterceptor requestInterceptor;
        private SynapseResponseInterceptor responseInterceptor;
        private SynapseRetryPolicy retryPolicy;
        private SynapseMetricsListener metricsListener;
        private int maxConcurrentRequests = 64;
        private int maxRequestsPerMinute = 0;
        private Duration circuitBreakerOpenDuration = Duration.ofSeconds(30);
        private int circuitBreakerFailureThreshold = 5;

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder temperature(double temperature) { this.temperature = temperature; return this; }
        public Builder maxTokens(int maxTokens) { this.maxTokens = maxTokens; return this; }
        public Builder connectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; return this; }
        public Builder readTimeout(Duration readTimeout) { this.readTimeout = readTimeout; return this; }
        public Builder timeout(Duration readTimeout) { this.readTimeout = readTimeout; return this; }
        public Builder maxRetries(int maxRetries) { this.maxRetries = maxRetries; return this; }
        public Builder retryDelay(Duration retryDelay) { this.retryDelay = retryDelay; return this; }
        public Builder requestTimeout(Duration requestTimeout) { this.requestTimeout = requestTimeout; return this; }
        public Builder streamIdleTimeout(Duration streamIdleTimeout) { this.streamIdleTimeout = streamIdleTimeout; return this; }
        public Builder enableLogging(boolean enableLogging) { this.enableLogging = enableLogging; return this; }
        public Builder maxRetryElapsedTime(Duration maxRetryElapsedTime) { this.maxRetryElapsedTime = maxRetryElapsedTime; return this; }
        public Builder requestInterceptor(SynapseRequestInterceptor i) { this.requestInterceptor = i; return this; }
        public Builder responseInterceptor(SynapseResponseInterceptor i) { this.responseInterceptor = i; return this; }
        public Builder retryPolicy(SynapseRetryPolicy p) { this.retryPolicy = p; return this; }
        public Builder metricsListener(SynapseMetricsListener l) { this.metricsListener = l; return this; }
        public Builder maxConcurrentRequests(int max) { this.maxConcurrentRequests = max; return this; }
        public Builder maxRequestsPerMinute(int max) { this.maxRequestsPerMinute = max; return this; }
        public Builder circuitBreakerOpenDuration(Duration d) { this.circuitBreakerOpenDuration = d; return this; }
        public Builder circuitBreakerFailureThreshold(int t) { this.circuitBreakerFailureThreshold = t; return this; }
        public SynapseConfig build() { return new SynapseConfig(this); }
    }
}
