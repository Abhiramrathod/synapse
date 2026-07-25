package org.abhi.synapse.config;

import org.abhi.synapse.core.AbstractSynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;
import org.abhi.synapse.interceptors.SynapseMetricsListener;

import java.time.Duration;

/**
 * Immutable configuration container for the Synapse HTTP client framework.
 *
 * <p>{@code SynapseConfig} holds all configuration parameters required to initialize
 * and operate a Synapse HTTP client, including connection details (base URL, endpoint,
 * API key), model selection, request tuning (temperature, max tokens, timeouts),
 * and interceptor hooks for custom request/response processing, retry behaviour,
 * and metrics collection.</p>
 *
 * <p>Instances are created via the {@link Builder} returned by {@link #builder()}.
 * The builder enforces a fluent API and delegates to {@link #validate()} for
 * required-field checks.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SynapseConfig config = SynapseConfig.builder()
 *         .baseUrl("https://api.openai.com")
 *         .endpoint("/v1/chat/completions")
 *         .apiKey("your-api-key")
 *         .modelName("gpt-4")
 *         .temperature(0.9)
 *         .maxTokens(2048)
 *         .timeout(Duration.ofSeconds(45))
 *         .maxRetries(5)
 *         .retryDelay(Duration.ofSeconds(1))
 *         .requestTimeout(Duration.ofSeconds(90))
 *         .enableLogging(true)
 *         .build();
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see AbstractSynapseConfig
 * @see org.abhi.synapse.interceptors.SynapseRequestInterceptor
 * @see org.abhi.synapse.interceptors.SynapseResponseInterceptor
 * @see org.abhi.synapse.interceptors.SynapseRetryPolicy
 * @see org.abhi.synapse.interceptors.SynapseMetricsListener
 */
public class SynapseConfig extends AbstractSynapseConfig {

    private String baseUrl;
    private String endpoint;
    private String apiKey;
    private String modelName;

    private SynapseRequestInterceptor requestInterceptor;
    private SynapseResponseInterceptor responseInterceptor;
    private SynapseRetryPolicy retryPolicy;
    private SynapseMetricsListener metricsListener;

    /**
     * Validates that all required configuration fields are present and non-blank.
     *
     * <p>The following fields are mandatory and must not be {@code null} or blank:</p>
     * <ul>
     *     <li>{@code baseUrl} &mdash; the base URL of the target API</li>
     *     <li>{@code endpoint} &mdash; the specific API endpoint path</li>
     *     <li>{@code apiKey} &mdash; the authentication key for the API</li>
     *     <li>{@code modelName} &mdash; the model identifier to use for requests</li>
     * </ul>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * SynapseConfig config = SynapseConfig.builder()
     *         .baseUrl("https://api.openai.com")
     *         .endpoint("/v1/chat/completions")
     *         .apiKey("sk-...")
     *         .modelName("gpt-4")
     *         .build();
     *
     * config.validate(); // throws SynapseException if any required field is missing
     * }</pre>
     *
     * @throws SynapseException if any required field is {@code null} or blank
     * @since 1.0.0
     */
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

    /**
     * Returns the base URL of the target API (for example, {@code "https://api.openai.com"}).
     *
     * @return the base URL, or {@code null} if not yet configured
     * @since 1.0.0
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL of the target API.
     *
     * @param baseUrl the base URL to connect to; must not be {@code null} or blank
     * @since 1.0.0
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the API endpoint path appended to the base URL
     * (for example, {@code "/v1/chat/completions"}).
     *
     * @return the endpoint path, or {@code null} if not yet configured
     * @since 1.0.0
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the API endpoint path that will be appended to the base URL.
     *
     * @param endpoint the endpoint path; must not be {@code null} or blank
     * @since 1.0.0
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the API key used to authenticate requests.
     *
     * @return the API key, or {@code null} if not yet configured
     * @since 1.0.0
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key used to authenticate requests.
     *
     * @param apiKey the API key; must not be {@code null} or blank
     * @since 1.0.0
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the model name used for API requests (for example, {@code "gpt-4"}).
     *
     * @return the model name, or {@code null} if not yet configured
     * @since 1.0.0
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the model name to use for API requests.
     *
     * @param modelName the model identifier; must not be {@code null} or blank
     * @since 1.0.0
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Returns the request interceptor that processes outbound HTTP requests
     * before they are sent.
     *
     * @return the {@link SynapseRequestInterceptor}, or {@code null} if none is configured
     * @since 1.0.0
     * @see #setRequestInterceptor(SynapseRequestInterceptor)
     */
    public SynapseRequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    /**
     * Sets a request interceptor that will be invoked before each outbound
     * HTTP request is sent. Use this to add custom headers, transform payloads,
     * or perform pre-flight validation.
     *
     * @param requestInterceptor the interceptor to apply; may be {@code null} to clear
     * @since 1.0.0
     * @see SynapseRequestInterceptor
     */
    public void setRequestInterceptor(SynapseRequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    /**
     * Returns the response interceptor that processes inbound HTTP responses
     * after they are received.
     *
     * @return the {@link SynapseResponseInterceptor}, or {@code null} if none is configured
     * @since 1.0.0
     * @see #setResponseInterceptor(SynapseResponseInterceptor)
     */
    public SynapseResponseInterceptor getResponseInterceptor() {
        return responseInterceptor;
    }

    /**
     * Sets a response interceptor that will be invoked after each inbound
     * HTTP response is received. Use this to transform response payloads,
     * log results, or perform post-processing.
     *
     * @param responseInterceptor the interceptor to apply; may be {@code null} to clear
     * @since 1.0.0
     * @see SynapseResponseInterceptor
     */
    public void setResponseInterceptor(SynapseResponseInterceptor responseInterceptor) {
        this.responseInterceptor = responseInterceptor;
    }

    /**
     * Returns the retry policy that governs automatic retry behaviour
     * on transient failures.
     *
     * @return the {@link SynapseRetryPolicy}, or {@code null} if none is configured
     * @since 1.0.0
     * @see #setRetryPolicy(SynapseRetryPolicy)
     */
    public SynapseRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Sets a retry policy that controls how failed requests are retried,
     * including back-off strategy and retryable status codes.
     *
     * @param retryPolicy the retry policy to apply; may be {@code null} to disable retries
     * @since 1.0.0
     * @see SynapseRetryPolicy
     */
    public void setRetryPolicy(SynapseRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * Returns the metrics listener that receives latency, throughput,
     * and error-rate observations.
     *
     * @return the {@link SynapseMetricsListener}, or {@code null} if none is configured
     * @since 1.0.0
     * @see #setMetricsListener(SynapseMetricsListener)
     */
    public SynapseMetricsListener getMetricsListener() {
        return metricsListener;
    }

    /**
     * Sets a metrics listener that will be notified of request-level
     * metrics such as latency, status codes, and error counts.
     *
     * @param metricsListener the listener to notify; may be {@code null} to clear
     * @since 1.0.0
     * @see SynapseMetricsListener
     */
    public void setMetricsListener(SynapseMetricsListener metricsListener) {
        this.metricsListener = metricsListener;
    }

    /**
     * Creates a new {@link Builder} for constructing {@code SynapseConfig} instances.
     *
     * <p>This is the recommended entry point for creating a configuration object.
     * All required and optional parameters can be set through the builder's
     * fluent API before calling {@link Builder#build()}.</p>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * SynapseConfig config = SynapseConfig.builder()
     *         .baseUrl("https://api.openai.com")
     *         .endpoint("/v1/chat/completions")
     *         .apiKey("your-api-key")
     *         .modelName("gpt-4")
     *         .build();
     * }</pre>
     *
     * @return a new {@link Builder} instance
     * @since 1.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for constructing {@link SynapseConfig} instances using a fluent API.
     *
     * <p>The builder allows all configuration parameters &mdash; both required fields
     * ({@code baseUrl}, {@code endpoint}, {@code apiKey}, {@code modelName}) and
     * optional tuning parameters &mdash; to be set in a readable, chainable manner.
     * Call {@link #build()} to obtain the final {@code SynapseConfig} instance.</p>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * SynapseConfig config = SynapseConfig.builder()
     *         .baseUrl("https://api.openai.com")
     *         .endpoint("/v1/chat/completions")
     *         .apiKey("your-api-key")
     *         .modelName("gpt-4")
     *         .temperature(0.5)
     *         .maxTokens(4096)
     *         .timeout(Duration.ofSeconds(60))
     *         .enableLogging(true)
     *         .build();
     * }</pre>
     *
     * @author Abhiram Rathod
     * @since 1.0.0
     * @see SynapseConfig#builder()
     */
    public static class Builder {
        private final SynapseConfig config = new SynapseConfig();

        /**
         * Sets the base URL of the target API.
         *
         * <p>This is a required field. The base URL should include the scheme
         * and host (for example, {@code "https://api.openai.com"}).</p>
         *
         * @param baseUrl the base URL; must not be {@code null} or blank
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder baseUrl(String baseUrl) {
            config.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the API endpoint path that will be appended to the base URL.
         *
         * <p>This is a required field. The endpoint should start with a slash
         * (for example, {@code "/v1/chat/completions"}).</p>
         *
         * @param endpoint the endpoint path; must not be {@code null} or blank
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder endpoint(String endpoint) {
            config.endpoint = endpoint;
            return this;
        }

        /**
         * Sets the API key used to authenticate requests.
         *
         * <p>This is a required field. The key will typically be passed as
         * a {@code Bearer} token in the {@code Authorization} header.</p>
         *
         * @param apiKey the API key; must not be {@code null} or blank
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder apiKey(String apiKey) {
            config.apiKey = apiKey;
            return this;
        }

        /**
         * Sets the model name used for API requests.
         *
         * <p>This is a required field. The value must be a valid model identifier
         * recognised by the target API (for example, {@code "gpt-4"} or
         * {@code "gpt-3.5-turbo"}).</p>
         *
         * @param modelName the model identifier; must not be {@code null} or blank
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder modelName(String modelName) {
            config.modelName = modelName;
            return this;
        }

        /**
         * Sets the sampling temperature for the model, controlling randomness.
         *
         * <p>Values typically range from {@code 0.0} (deterministic) to {@code 2.0}
         * (most random). Defaults to {@code 0.7} if not specified.</p>
         *
         * @param temperature the temperature value; must be between {@code 0.0} and {@code 2.0}
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder temperature(double temperature) {
            config.temperature = temperature;
            return this;
        }

        /**
         * Sets the maximum number of tokens to generate in the model response.
         *
         * <p>Defaults to {@code 1024} if not specified. The maximum allowed value
         * depends on the model being used.</p>
         *
         * @param maxTokens the maximum token count; must be a positive integer
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder maxTokens(int maxTokens) {
            config.maxTokens = maxTokens;
            return this;
        }

        /**
         * Sets the overall connection timeout for the HTTP client.
         *
         * <p>Defaults to {@code 30 seconds} if not specified.</p>
         *
         * @param timeout the connection timeout duration; must not be {@code null}
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder timeout(Duration timeout) {
            config.timeout = timeout;
            return this;
        }

        /**
         * Sets the maximum number of retries for failed requests.
         *
         * <p>Defaults to {@code 3} if not specified. Set to {@code 0} to
         * disable automatic retries.</p>
         *
         * @param maxRetries the maximum retry count; must be non-negative
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder maxRetries(int maxRetries) {
            config.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets the delay between successive retry attempts.
         *
         * <p>Defaults to {@code 500 milliseconds} if not specified. The actual
         * delay may be adjusted by the configured {@link SynapseRetryPolicy}.</p>
         *
         * @param retryDelay the delay between retries; must not be {@code null}
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder retryDelay(Duration retryDelay) {
            config.retryDelay = retryDelay;
            return this;
        }

        /**
         * Sets the timeout for individual HTTP requests.
         *
         * <p>Defaults to {@code 60 seconds} if not specified. This controls
         * how long the client waits for a response before timing out.</p>
         *
         * @param requestTimeout the request timeout duration; must not be {@code null}
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder requestTimeout(Duration requestTimeout) {
            config.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Enables or disables request/response logging.
         *
         * <p>Defaults to {@code true} if not specified.</p>
         *
         * @param enableLogging {@code true} to enable logging, {@code false} to disable
         * @return this builder for method chaining
         * @since 1.0.0
         */
        public Builder enableLogging(boolean enableLogging) {
            config.enableLogging = enableLogging;
            return this;
        }

        /**
         * Sets a request interceptor for pre-processing outbound HTTP requests.
         *
         * <p>The interceptor is invoked before each request is sent, allowing
         * custom headers, payload transformations, or validation logic to be
         * applied.</p>
         *
         * @param interceptor the {@link SynapseRequestInterceptor} to apply; may be {@code null}
         * @return this builder for method chaining
         * @since 1.0.0
         * @see SynapseRequestInterceptor
         */
        public Builder requestInterceptor(SynapseRequestInterceptor interceptor) {
            config.requestInterceptor = interceptor;
            return this;
        }

        /**
         * Sets a response interceptor for post-processing inbound HTTP responses.
         *
         * <p>The interceptor is invoked after each response is received, allowing
         * payload transformation, logging, or custom error handling.</p>
         *
         * @param interceptor the {@link SynapseResponseInterceptor} to apply; may be {@code null}
         * @return this builder for method chaining
         * @since 1.0.0
         * @see SynapseResponseInterceptor
         */
        public Builder responseInterceptor(SynapseResponseInterceptor interceptor) {
            config.responseInterceptor = interceptor;
            return this;
        }

        /**
         * Sets a retry policy for handling transient request failures.
         *
         * <p>The policy controls retry count, back-off strategy, and which
         * HTTP status codes or exceptions are considered retryable.</p>
         *
         * @param policy the {@link SynapseRetryPolicy} to apply; may be {@code null} to disable retries
         * @return this builder for method chaining
         * @since 1.0.0
         * @see SynapseRetryPolicy
         */
        public Builder retryPolicy(SynapseRetryPolicy policy) {
            config.retryPolicy = policy;
            return this;
        }

        /**
         * Sets a metrics listener for observing request-level telemetry.
         *
         * <p>The listener receives callbacks for latency, throughput, error
         * counts, and other operational metrics.</p>
         *
         * @param listener the {@link SynapseMetricsListener} to notify; may be {@code null}
         * @return this builder for method chaining
         * @since 1.0.0
         * @see SynapseMetricsListener
         */
        public Builder metricsListener(SynapseMetricsListener listener) {
            config.metricsListener = listener;
            return this;
        }

        /**
         * Builds and returns the configured {@link SynapseConfig} instance.
         *
         * <p>This method finalises the builder and returns the immutable
         * configuration object. Note that this does <em>not</em> call
         * {@link SynapseConfig#validate()} &mdash; callers should invoke
         * validation explicitly if required.</p>
         *
         * <p>Usage example:</p>
         * <pre>{@code
         * SynapseConfig config = SynapseConfig.builder()
         *         .baseUrl("https://api.openai.com")
         *         .endpoint("/v1/chat/completions")
         *         .apiKey("your-api-key")
         *         .modelName("gpt-4")
         *         .build();
         * }</pre>
         *
         * @return a fully constructed {@link SynapseConfig} instance
         * @since 1.0.0
         */
        public SynapseConfig build() {
            return config;
        }
    }
}
