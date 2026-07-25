package org.abhi.synapse.core;

import java.time.Duration;

/**
 * Abstract base class providing common configuration properties for Synapse LLM clients.
 *
 * <p>{@code AbstractSynapseConfig} serves as the foundation for provider-specific configuration
 * classes (e.g., OpenAI, Anthropic). It encapsulates shared settings such as temperature,
 * token limits, timeout durations, retry behavior, and logging preferences that are common
 * across all LLM providers.</p>
 *
 * <p>Subclasses should extend this class to add provider-specific configuration properties
 * such as API keys, model names, base URLs, and custom headers.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * OpenAIConfig config = new OpenAIConfig();
 * config.setTemperature(0.9);
 * config.setMaxTokens(2048);
 * config.setTimeout(Duration.ofSeconds(45));
 * config.setMaxRetries(5);
 *
 * ISynapseHub hub = synapseFactory.createHub(config);
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see ISynapseHub
 */
public abstract class AbstractSynapseConfig {

    /** Default temperature for sampling. Controls randomness: lower is more deterministic. */
    protected static final double DEFAULT_TEMPERATURE = 0.7;

    /** Default maximum number of tokens the LLM can generate in a response. */
    protected static final int DEFAULT_MAX_TOKENS = 1024;

    /** Default timeout for individual LLM API operations. */
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /** Default maximum number of retry attempts for transient failures. */
    protected static final int DEFAULT_MAX_RETRIES = 3;

    /** Default delay between retry attempts. */
    protected static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(500);

    /** Default overall request timeout including all retries. */
    protected static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);

    /** Default setting for whether request/response logging is enabled. */
    protected static final boolean DEFAULT_ENABLE_LOGGING = true;

    /** Temperature for LLM sampling. Higher values produce more random output. */
    protected double temperature = DEFAULT_TEMPERATURE;
    /** Maximum number of tokens the LLM should generate in its response. */
    protected int maxTokens = DEFAULT_MAX_TOKENS;
    /** HTTP connection and read timeout for a single request attempt. */
    protected Duration timeout = DEFAULT_TIMEOUT;
    /** Maximum number of retry attempts on transient failures. */
    protected int maxRetries = DEFAULT_MAX_RETRIES;
    /** Delay between retry attempts. */
    protected Duration retryDelay = DEFAULT_RETRY_DELAY;
    /** Overall request timeout including all retries. */
    protected Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    /** Whether request/response logging is enabled. */
    protected boolean enableLogging = DEFAULT_ENABLE_LOGGING;

    /**
     * Returns the temperature setting for LLM sampling.
     *
     * <p>Temperature controls the randomness of the output. Values typically range from 0.0
     * (deterministic) to 2.0 (highly random). A value of 0.7 provides a balance between
     * creativity and coherence.</p>
     *
     * @return the current temperature value
     * @since 1.0.0
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature setting for LLM sampling.
     *
     * @param temperature the temperature value, typically between 0.0 and 2.0
     * @since 1.0.0
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Returns the maximum number of tokens the LLM can generate in a response.
     *
     * @return the current maximum token limit
     * @since 1.0.0
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Sets the maximum number of tokens the LLM can generate in a response.
     *
     * @param maxTokens the maximum token count; must be a positive integer
     * @since 1.0.0
     */
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * Returns the timeout duration for individual LLM API operations.
     *
     * @return the current timeout duration
     * @since 1.0.0
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout duration for individual LLM API operations.
     *
     * @param timeout the timeout duration; must not be null
     * @since 1.0.0
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the maximum number of retry attempts for transient failures.
     *
     * @return the current maximum retry count
     * @since 1.0.0
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Sets the maximum number of retry attempts for transient failures.
     *
     * <p>Retries are attempted for rate limit errors (HTTP 429), server errors (HTTP 5xx),
     * network errors, and timeout errors.</p>
     *
     * @param maxRetries the maximum number of retries; must be non-negative
     * @since 1.0.0
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Returns the delay between retry attempts.
     *
     * @return the current retry delay duration
     * @since 1.0.0
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * Sets the delay between retry attempts.
     *
     * @param retryDelay the delay duration between retries; must not be null
     * @since 1.0.0
     */
    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Returns the overall request timeout including all retry attempts.
     *
     * @return the current request timeout duration
     * @since 1.0.0
     */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the overall request timeout including all retry attempts.
     *
     * <p>This timeout encompasses the entire lifecycle of a request, including any retries.
     * If exceeded, the request is abandoned regardless of remaining retry attempts.</p>
     *
     * @param requestTimeout the overall request timeout duration; must not be null
     * @since 1.0.0
     */
    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    /**
     * Returns whether request/response logging is enabled.
     *
     * @return {@code true} if logging is enabled, {@code false} otherwise
     * @since 1.0.0
     */
    public boolean isEnableLogging() {
        return enableLogging;
    }

    /**
     * Sets whether request/response logging is enabled.
     *
     * <p>When enabled, the client will log request payloads, response bodies, and timing
     * information at the DEBUG level. Sensitive data such as API keys is never logged.</p>
     *
     * @param enableLogging {@code true} to enable logging, {@code false} to disable
     * @since 1.0.0
     */
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
