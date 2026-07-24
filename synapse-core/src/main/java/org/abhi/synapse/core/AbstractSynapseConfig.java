package org.abhi.synapse.core;

import java.time.Duration;

public abstract class AbstractSynapseConfig {

    protected static final double DEFAULT_TEMPERATURE = 0.7;
    protected static final int DEFAULT_MAX_TOKENS = 1024;
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    protected static final int DEFAULT_MAX_RETRIES = 3;
    protected static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(500);
    protected static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    protected static final boolean DEFAULT_ENABLE_LOGGING = true;

    protected double temperature = DEFAULT_TEMPERATURE;
    protected int maxTokens = DEFAULT_MAX_TOKENS;
    protected Duration timeout = DEFAULT_TIMEOUT;
    protected int maxRetries = DEFAULT_MAX_RETRIES;
    protected Duration retryDelay = DEFAULT_RETRY_DELAY;
    protected Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    protected boolean enableLogging = DEFAULT_ENABLE_LOGGING;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
