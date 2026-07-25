package org.abhi.synapse.spring;

import org.abhi.synapse.config.SynapseConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Spring Boot configuration properties for Synapse.
 *
 * <p>Binds properties from the {@code synapse.*} prefix in {@code application.properties}
 * or {@code application.yml} to strongly-typed fields. Use {@link #toSynapseConfig()} to
 * convert these properties into a {@link SynapseConfig} instance consumed by the core library.</p>
 *
 * <p>Example configuration:</p>
 * <pre>{@code
 * synapse.api-key=sk-...
 * synapse.model-name=gpt-4
 * synapse.base-url=https://api.openai.com
 * synapse.endpoint=/v1/chat/completions
 * synapse.temperature=0.7
 * synapse.max-tokens=2048
 * synapse.timeout=30s
 * synapse.max-retries=3
 * synapse.retry-delay=1s
 * synapse.request-timeout=60s
 * synapse.enable-logging=true
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseConfig
 * @see SynapseAutoConfiguration
 */
@ConfigurationProperties(prefix = "synapse")
public class SynapseProperties {

    /** The base URL of the target API (e.g. {@code "https://api.openai.com"}). */
    private String baseUrl;

    /** The API endpoint path appended to the base URL (e.g. {@code "/v1/chat/completions"}). */
    private String endpoint;

    /** The API key used to authenticate requests. */
    private String apiKey;

    /** The model name to use for completions (e.g. {@code "gpt-4"}). */
    private String modelName;

    /** Sampling temperature controlling randomness (0.0 - 2.0). */
    private double temperature;

    /** Maximum number of tokens to generate in each completion. */
    private int maxTokens;

    /** Overall timeout for HTTP operations. */
    private Duration timeout;

    /** Maximum number of retry attempts on transient failures. */
    private int maxRetries;

    /** Delay between retry attempts. */
    private Duration retryDelay;

    /** Timeout for individual HTTP requests. */
    private Duration requestTimeout;

    /** Whether request/response logging is enabled. */
    private boolean enableLogging;

    /**
     * Converts these properties into a {@link SynapseConfig} instance.
     *
     * <p>Delegates to {@link SynapseConfig.Builder} to construct an immutable
     * configuration object using all configured property values.</p>
     *
     * @return a fully-built {@link SynapseConfig} instance
     * @since 1.0.0
     */
    public SynapseConfig toSynapseConfig() {
        SynapseConfig.Builder builder = SynapseConfig.builder()
                .baseUrl(baseUrl)
                .endpoint(endpoint)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(timeout)
                .maxRetries(maxRetries)
                .retryDelay(retryDelay)
                .requestTimeout(requestTimeout)
                .enableLogging(enableLogging);
        return builder.build();
    }

    /**
     * Returns the base URL of the target API.
     *
     * @return the base URL, or {@code null} if not configured
     * @since 1.0.0
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL of the target API (e.g. {@code "https://api.openai.com"}).
     *
     * @param baseUrl the base URL to set
     * @since 1.0.0
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the API endpoint path.
     *
     * @return the endpoint path, or {@code null} if not configured
     * @since 1.0.0
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the API endpoint path appended to the base URL (e.g. {@code "/v1/chat/completions"}).
     *
     * @param endpoint the endpoint path to set
     * @since 1.0.0
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the API key used to authenticate requests.
     *
     * @return the API key, or {@code null} if not configured
     * @since 1.0.0
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key used to authenticate requests.
     *
     * @param apiKey the API key to set
     * @since 1.0.0
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the model name used for completions.
     *
     * @return the model name, or {@code null} if not configured
     * @since 1.0.0
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the model name to use for completions (e.g. {@code "gpt-4"}).
     *
     * @param modelName the model name to set
     * @since 1.0.0
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Returns the sampling temperature controlling randomness.
     *
     * @return the temperature value (0.0 - 2.0)
     * @since 1.0.0
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Sets the sampling temperature controlling randomness (0.0 - 2.0).
     *
     * @param temperature the temperature value to set
     * @since 1.0.0
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Returns the maximum number of tokens to generate per completion.
     *
     * @return the maximum token count
     * @since 1.0.0
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Sets the maximum number of tokens to generate per completion.
     *
     * @param maxTokens the maximum token count to set
     * @since 1.0.0
     */
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * Returns the overall timeout for HTTP operations.
     *
     * @return the timeout duration, or {@code null} if not configured
     * @since 1.0.0
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Sets the overall timeout for HTTP operations.
     *
     * @param timeout the timeout duration to set
     * @since 1.0.0
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the maximum number of retry attempts on transient failures.
     *
     * @return the maximum retry count
     * @since 1.0.0
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Sets the maximum number of retry attempts on transient failures.
     *
     * @param maxRetries the maximum retry count to set
     * @since 1.0.0
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Returns the delay between retry attempts.
     *
     * @return the retry delay duration, or {@code null} if not configured
     * @since 1.0.0
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * Sets the delay between retry attempts.
     *
     * @param retryDelay the retry delay duration to set
     * @since 1.0.0
     */
    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }

    /**
     * Returns the timeout for individual HTTP requests.
     *
     * @return the request timeout duration, or {@code null} if not configured
     * @since 1.0.0
     */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the timeout for individual HTTP requests.
     *
     * @param requestTimeout the request timeout duration to set
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
     * @param enableLogging {@code true} to enable logging, {@code false} to disable
     * @since 1.0.0
     */
    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
