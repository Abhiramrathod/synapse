package org.abhi.synapse.core;

import java.time.Duration;

public abstract class AbstractSynapseConfig {

    protected static final double DEFAULT_TEMPERATURE = 0.7;
    protected static final int DEFAULT_MAX_TOKENS = 1024;
    protected static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    protected static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    protected static final int DEFAULT_MAX_RETRIES = 3;
    protected static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(500);
    protected static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);
    protected static final Duration DEFAULT_STREAM_IDLE_TIMEOUT = Duration.ofSeconds(120);
    protected static final boolean DEFAULT_ENABLE_LOGGING = true;

    protected double temperature = DEFAULT_TEMPERATURE;
    protected int maxTokens = DEFAULT_MAX_TOKENS;
    protected Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    protected Duration readTimeout = DEFAULT_READ_TIMEOUT;
    protected Duration timeout = DEFAULT_TIMEOUT;
    protected int maxRetries = DEFAULT_MAX_RETRIES;
    protected Duration retryDelay = DEFAULT_RETRY_DELAY;
    protected Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    protected Duration streamIdleTimeout = DEFAULT_STREAM_IDLE_TIMEOUT;
    protected boolean enableLogging = DEFAULT_ENABLE_LOGGING;

    protected AbstractSynapseConfig() {}

    protected AbstractSynapseConfig(double temperature, int maxTokens, Duration connectTimeout,
                                     Duration readTimeout, int maxRetries, Duration retryDelay,
                                     Duration requestTimeout, Duration streamIdleTimeout,
                                     boolean enableLogging, Duration maxRetryElapsedTime) {
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.timeout = readTimeout;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.requestTimeout = requestTimeout;
        this.streamIdleTimeout = streamIdleTimeout;
        this.enableLogging = enableLogging;
    }

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
}
