package org.abhi.synapse.config;

import org.abhi.synapse.core.AbstractSynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

import java.time.Duration;

public class SynapseConfig extends AbstractSynapseConfig {

    private String baseUrl;
    private String endpoint;
    private String apiKey;
    private String modelName;

    private SynapseRequestInterceptor requestInterceptor;
    private SynapseResponseInterceptor responseInterceptor;
    private SynapseRetryPolicy retryPolicy;
    private SynapseMetricsListener metricsListener;

    public void validate() throws SynapseException {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new SynapseException("baseUrl is required");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new SynapseException("endpoint is required");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new SynapseException("apiKey is required");
        }
        if (modelName == null || modelName.isBlank()) {
            throw new SynapseException("modelName is required");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public SynapseRequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    public void setRequestInterceptor(SynapseRequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    public SynapseResponseInterceptor getResponseInterceptor() {
        return responseInterceptor;
    }

    public void setResponseInterceptor(SynapseResponseInterceptor responseInterceptor) {
        this.responseInterceptor = responseInterceptor;
    }

    public SynapseRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(SynapseRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public SynapseMetricsListener getMetricsListener() {
        return metricsListener;
    }

    public void setMetricsListener(SynapseMetricsListener metricsListener) {
        this.metricsListener = metricsListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SynapseConfig config = new SynapseConfig();

        public Builder baseUrl(String baseUrl) {
            config.baseUrl = baseUrl;
            return this;
        }

        public Builder endpoint(String endpoint) {
            config.endpoint = endpoint;
            return this;
        }

        public Builder apiKey(String apiKey) {
            config.apiKey = apiKey;
            return this;
        }

        public Builder modelName(String modelName) {
            config.modelName = modelName;
            return this;
        }

        public Builder temperature(double temperature) {
            config.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            config.maxTokens = maxTokens;
            return this;
        }

        public Builder timeout(Duration timeout) {
            config.timeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            config.maxRetries = maxRetries;
            return this;
        }

        public Builder retryDelay(Duration retryDelay) {
            config.retryDelay = retryDelay;
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            config.requestTimeout = requestTimeout;
            return this;
        }

        public Builder enableLogging(boolean enableLogging) {
            config.enableLogging = enableLogging;
            return this;
        }

        public Builder requestInterceptor(SynapseRequestInterceptor interceptor) {
            config.requestInterceptor = interceptor;
            return this;
        }

        public Builder responseInterceptor(SynapseResponseInterceptor interceptor) {
            config.responseInterceptor = interceptor;
            return this;
        }

        public Builder retryPolicy(SynapseRetryPolicy policy) {
            config.retryPolicy = policy;
            return this;
        }

        public Builder metricsListener(SynapseMetricsListener listener) {
            config.metricsListener = listener;
            return this;
        }

        public SynapseConfig build() {
            return config;
        }
    }
}
