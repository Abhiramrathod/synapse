package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.TokenProvider;

import java.time.Duration;

/**
 * Mutable, thread-safe snapshot of the runtime-tunable hub settings.
 *
 * <p>Request paths read these volatile fields on every call, so updating them
 * (e.g. {@code hub.updateApiKey(...)}) takes effect immediately on the next
 * request without destroying the shared {@link java.net.http.HttpClient}
 * connection pool or the async executor. Fields not listed here (interceptors,
 * listeners, retry policy, circuit breaker) remain fixed for the hub's
 * lifetime.</p>
 *
 * <p>This is an internal class within the {@code synapse-http} module.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 */
final class HubSettings {

    volatile String apiKey;
    volatile TokenProvider tokenProvider;
    volatile String baseUrl;
    volatile String endpoint;
    volatile String modelName;
    volatile Duration requestTimeout;
    volatile double temperature;
    volatile int maxTokens;
    volatile boolean enableLogging;

    HubSettings(SynapseConfig config) {
        updateFrom(config);
    }

    void updateFrom(SynapseConfig config) {
        this.apiKey = config.getApiKey();
        this.tokenProvider = config.getTokenProvider();
        this.baseUrl = config.getBaseUrl();
        this.endpoint = config.getEndpoint();
        this.modelName = config.getModelName();
        this.requestTimeout = config.getRequestTimeout();
        this.temperature = config.getTemperature();
        this.maxTokens = config.getMaxTokens();
        this.enableLogging = config.isEnableLogging();
    }
}
