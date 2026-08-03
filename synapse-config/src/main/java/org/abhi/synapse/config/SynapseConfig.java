package org.abhi.synapse.config;

import org.abhi.synapse.core.AbstractSynapseConfig;
import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.TokenProvider;
import org.abhi.synapse.core.cache.ResponseCache;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.interceptors.SynapseRetryPolicy;
import org.abhi.synapse.interceptors.SynapseMetricsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class SynapseConfig extends AbstractSynapseConfig {

    private static final Logger log = LoggerFactory.getLogger(SynapseConfig.class);

    private final String baseUrl;
    private final String endpoint;
    private final String apiKey;
    private final TokenProvider tokenProvider;
    private final String modelName;
    private final String provider;
    private final ProviderAdapter providerAdapter;
    private final SynapseRequestInterceptor requestInterceptor;
    private final SynapseResponseInterceptor responseInterceptor;
    private final SynapseRetryPolicy retryPolicy;
    private final SynapseMetricsListener metricsListener;
    private final int maxConcurrentRequests;
    private final int maxRequestsPerMinute;
    private final int maxToolIterations;
    private final Duration circuitBreakerOpenDuration;
    private final int circuitBreakerFailureThreshold;
    private final Proxy proxy;
    private final SSLContext sslContext;
    private final boolean trustAll;
    private final ResponseCache responseCache;

    private SynapseConfig(Builder builder) {
        super(builder.temperature, builder.maxTokens, builder.connectTimeout,
              builder.readTimeout, builder.maxRetries, builder.retryDelay,
              builder.requestTimeout, builder.streamIdleTimeout, builder.enableLogging,
              builder.maxRetryElapsedTime);
        this.baseUrl = builder.baseUrl;
        this.endpoint = builder.endpoint;
        this.apiKey = builder.apiKey;
        this.tokenProvider = builder.tokenProvider;
        this.modelName = builder.modelName;
        this.provider = builder.provider;
        this.providerAdapter = builder.providerAdapter;
        this.requestInterceptor = builder.requestInterceptor;
        this.responseInterceptor = builder.responseInterceptor;
        this.retryPolicy = builder.retryPolicy;
        this.metricsListener = builder.metricsListener;
        this.maxConcurrentRequests = builder.maxConcurrentRequests;
        this.maxRequestsPerMinute = builder.maxRequestsPerMinute;
        this.maxToolIterations = builder.maxToolIterations;
        this.circuitBreakerOpenDuration = builder.circuitBreakerOpenDuration;
        this.circuitBreakerFailureThreshold = builder.circuitBreakerFailureThreshold;
        this.proxy = builder.proxy;
        this.sslContext = builder.sslContext;
        this.trustAll = builder.trustAll;
        this.responseCache = builder.responseCache;
    }

    public void validate() throws SynapseException {
        if (baseUrl == null || baseUrl.isBlank()) throw new SynapseException("baseUrl is required");
        if (endpoint == null || endpoint.isBlank()) throw new SynapseException("endpoint is required");
        if ((apiKey == null || apiKey.isBlank()) && tokenProvider == null) {
            throw new SynapseException("apiKey is required when no tokenProvider is configured");
        }
        if (modelName == null || modelName.isBlank()) throw new SynapseException("modelName is required");
        if (providerAdapter == null && (provider == null || provider.isBlank())) {
            throw new SynapseException("provider is required when no ProviderAdapter is injected");
        }
    }

    public String getBaseUrl() { return baseUrl; }
    public String getEndpoint() { return endpoint; }
    public String getApiKey() { return apiKey; }
    public TokenProvider getTokenProvider() { return tokenProvider; }
    public String getModelName() { return modelName; }
    public String getProvider() { return provider; }
    public ProviderAdapter getProviderAdapter() { return providerAdapter; }
    public SynapseRequestInterceptor getRequestInterceptor() { return requestInterceptor; }
    public SynapseResponseInterceptor getResponseInterceptor() { return responseInterceptor; }
    public SynapseRetryPolicy getRetryPolicy() { return retryPolicy; }
    public SynapseMetricsListener getMetricsListener() { return metricsListener; }
    public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
    public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
    public int getMaxToolIterations() { return maxToolIterations; }
    public Duration getCircuitBreakerOpenDuration() { return circuitBreakerOpenDuration; }
    public int getCircuitBreakerFailureThreshold() { return circuitBreakerFailureThreshold; }
    public Proxy getProxy() { return proxy; }
    public SSLContext getSslContext() { return sslContext; }
    public boolean isTrustAll() { return trustAll; }
    public ResponseCache getResponseCache() { return responseCache; }

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
        private TokenProvider tokenProvider;
        private String modelName;
        private String provider = "openai";
        private ProviderAdapter providerAdapter;
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
        private int maxToolIterations = 10;
        private Duration circuitBreakerOpenDuration = Duration.ofSeconds(30);
        private int circuitBreakerFailureThreshold = 5;
        private Proxy proxy;
        private SSLContext sslContext;
        private boolean trustAll = false;
        private ResponseCache responseCache;

        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }

        /**
         * Supplies the authentication credential dynamically on every request.
         *
         * <p>When set, {@link #apiKey(String)} becomes optional. Use this for
         * rotating tokens, AWS SigV4, or Azure Entra ID / Managed Identity.</p>
         *
         * @param tokenProvider the token source; must not be {@code null}
         * @return this builder
         */
        public Builder tokenProvider(TokenProvider tokenProvider) {
            if (tokenProvider == null) throw new IllegalArgumentException("tokenProvider must not be null");
            this.tokenProvider = tokenProvider;
            return this;
        }

        /** Alias for {@link #tokenProvider(TokenProvider)} adapting a plain supplier. */
        public Builder tokenProvider(java.util.function.Supplier<String> supplier) {
            return tokenProvider(TokenProvider.fromSupplier(supplier));
        }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder provider(ProviderAdapter adapter) { this.providerAdapter = adapter; return this; }
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
        public Builder maxToolIterations(int max) { this.maxToolIterations = max; return this; }
        public Builder circuitBreakerOpenDuration(Duration d) { this.circuitBreakerOpenDuration = d; return this; }
        public Builder circuitBreakerFailureThreshold(int t) { this.circuitBreakerFailureThreshold = t; return this; }

        /**
         * Routes all outbound requests through the given {@link Proxy}.
         *
         * @param proxy the proxy to use; pass {@code null} to use direct connections
         * @return this builder
         */
        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * Attaches a {@link ResponseCache} so repeated prompts are served from
         * the cache instead of the provider.
         *
         * <p>Built-in adapters live in the {@code synapse-cache} module
         * ({@code CaffeineResponseCache}, {@code RedisResponseCache}).</p>
         *
         * @param responseCache the cache to use; pass {@code null} to disable caching
         * @return this builder
         */
        public Builder cache(ResponseCache responseCache) {
            this.responseCache = responseCache;
            return this;
        }

        /** Alias for {@link #cache(ResponseCache)}. */
        public Builder responseCache(ResponseCache responseCache) {
            return cache(responseCache);
        }

        /**
         * Uses the given {@link SSLContext} for all outbound HTTPS requests,
         * overriding the JVM default trust material.
         *
         * @param sslContext the SSL context to use; must not be {@code null}
         * @return this builder
         */
        public Builder sslContext(SSLContext sslContext) {
            if (sslContext == null) throw new IllegalArgumentException("sslContext must not be null");
            this.sslContext = sslContext;
            return this;
        }

        /**
         * Configures the SSL context to trust the certificates in the given
         * trust store file ({@code PKCS12} or {@code JKS}).
         *
         * @param path     path to the trust store file; must not be {@code null}
         * @param password the trust store password; must not be {@code null}
         * @return this builder
         * @throws IllegalArgumentException if the trust store cannot be loaded
         */
        public Builder trustStore(Path path, String password) {
            if (path == null) throw new IllegalArgumentException("trust store path must not be null");
            try {
                this.sslContext = loadTrustSslContext(path, password);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to load trust store from " + path + ": " + e.getMessage(), e);
            }
            return this;
        }

        /**
         * Disables TLS certificate validation entirely.
         *
         * <p>Only use this in development/test environments where no certificate
         * authority can verify the endpoint. Enabling it logs a loud warning.</p>
         *
         * @param trustAll {@code true} to accept any server certificate
         * @return this builder
         */
        public Builder trustAll(boolean trustAll) {
            if (trustAll) {
                log.warn("[Synapse] trustAll(true) configured: TLS certificate validation is DISABLED. "
                        + "Never use this in production.");
                this.sslContext = createTrustAllSslContext();
            }
            this.trustAll = trustAll;
            return this;
        }

        private static SSLContext loadTrustSslContext(Path path, String password) throws Exception {
            char[] pwd = password.toCharArray();
            for (String type : new String[]{"PKCS12", "JKS"}) {
                try (InputStream in = Files.newInputStream(path)) {
                    KeyStore keyStore = KeyStore.getInstance(type);
                    keyStore.load(in, pwd);
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(keyStore);
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);
                    return context;
                } catch (IOException keystoreFormatMismatch) {
                    // try the next store type
                }
            }
            throw new IllegalArgumentException("Unsupported keystore format: " + path);
        }

        private static SSLContext createTrustAllSslContext() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                    @Override public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                    @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }};
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                return context;
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Failed to create trust-all SSLContext", e);
            }
        }

        public SynapseConfig build() { return new SynapseConfig(this); }
    }
}
