package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.StreamHandle;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.TokenProvider;
import org.abhi.synapse.core.cache.ResponseCache;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.ToolCall;
import org.abhi.synapse.core.CancellationToken;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.ResponseFormat;
import org.abhi.synapse.core.model.SynapseRequestContext;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.SynapseResponseContext;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.metrics.SynapseMetrics;
import org.abhi.synapse.metrics.SynapseMetricsCollector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.function.BiConsumer;

public class SynapseHub implements ISynapseHub, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SynapseHub.class);

    private final SynapseConfig config;
    private final HubSettings settings;
    private final ProviderAdapter adapter;
    private final SynapseRequestBuilder requestBuilder;
    private final SynapseResponseParser responseParser;
    private final SynapseHttpClient httpClient;
    private final SynapseStreamHandler streamHandler;
    private final SynapseRetryHandler retryHandler;
    private final SynapseMetricsCollector metricsCollector;
    private final SynapseMetrics metrics;
    private final ObjectMapper objectMapper;
    private final ExecutorService asyncExecutor;
    private final CircuitBreaker circuitBreaker;
    private final ConcurrencyLimiter concurrencyLimiter;
    private final RateLimiter rateLimiter;
    private volatile boolean closed = false;

    public SynapseHub(SynapseConfig config) {
        this(config, new ObjectMapper());
    }

    public SynapseHub(SynapseConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.settings = new HubSettings(config);
        this.objectMapper = SynapseJson.configure(objectMapper);
        this.metrics = new SynapseMetrics();
        this.adapter = resolveAdapter(config);
        this.requestBuilder = new SynapseRequestBuilder(settings, objectMapper, adapter);
        this.responseParser = new SynapseResponseParser(adapter);
        HttpClient.Builder httpBuilder = HttpClient.newBuilder().connectTimeout(config.getTimeout());
        if (config.getSslContext() != null) {
            httpBuilder.sslContext(config.getSslContext());
        }
        if (config.getProxy() != null) {
            Proxy proxy = config.getProxy();
            httpBuilder.proxy(new ProxySelector() {
                @Override public java.util.List<Proxy> select(java.net.URI uri) {
                    return java.util.List.of(proxy);
                }
                @Override public void connectFailed(java.net.URI uri, java.net.SocketAddress sa,
                                                    java.io.IOException ioe) {
                    log.debug("[Synapse] Proxy connect failed for {}: {}", uri, ioe.getMessage());
                }
            });
        }
        this.httpClient = new SynapseHttpClient(httpBuilder.build());
        this.streamHandler = new SynapseStreamHandler(httpClient, adapter);
        this.retryHandler = new SynapseRetryHandler(config);
        this.metricsCollector = new SynapseMetricsCollector(metrics, config);
        this.circuitBreaker = new CircuitBreaker(
                config.getCircuitBreakerFailureThreshold(),
                config.getCircuitBreakerOpenDuration());
        this.concurrencyLimiter = new ConcurrencyLimiter(config.getMaxConcurrentRequests());
        this.rateLimiter = config.getMaxRequestsPerMinute() > 0
                ? new RateLimiter(config.getMaxRequestsPerMinute(), java.time.Duration.ofMinutes(1))
                : null;
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "synapse-async-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });
        validateConfig();
        log.info("[Synapse] Hub initialized for model: {}", settings.modelName);
    }

    private void validateConfig() {
        try {
            config.validate();
        } catch (SynapseException e) {
            throw new IllegalArgumentException("Invalid SynapseConfig: " + e.getMessage());
        }
    }

    private static ProviderAdapter resolveAdapter(SynapseConfig config) {
        ProviderAdapter explicit = config.getProviderAdapter();
        if (explicit != null) {
            return explicit;
        }
        String provider = config.getProvider();
        List<ProviderAdapter> adapters = new ArrayList<>();
        ServiceLoader.load(ProviderAdapter.class).forEach(adapters::add);
        for (ProviderAdapter candidate : adapters) {
            if (candidate.providerName().equalsIgnoreCase(provider)) {
                return candidate;
            }
        }
        String found = adapters.stream()
                .map(ProviderAdapter::providerName)
                .collect(java.util.stream.Collectors.joining(", "));
        throw new IllegalArgumentException("No ProviderAdapter registered for provider '" + provider
                + "'. Registered providers: " + (found.isEmpty() ? "(none)" : found));
    }

    @Override
    public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
        checkNotClosed();
        ResponseCache cache = config.getResponseCache();
        if (cache != null) {
            String key = cacheKey(prompt, options);
            java.util.Optional<SynapseResponse> hit = cache.get(key);
            if (hit.isPresent()) {
                log.debug("[Synapse] Cache hit for prompt");
                return hit.get();
            }
            SynapseResponse response = sendChat(List.of(ChatMessage.user(prompt)), options);
            cache.put(key, response);
            return response;
        }
        return sendChat(List.of(ChatMessage.user(prompt)), options);
    }

    private String cacheKey(String prompt, RequestOptions options) {
        return resolveModel(options) + "|" + prompt;
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        checkNotClosed();
        return sendChatWithToolLoop(messages, options, 0);
    }

    private SynapseResponse sendChatWithToolLoop(List<ChatMessage> messages, RequestOptions options, int iteration)
            throws SynapseException {
        String modelName = resolveModel(options);
        List<ChatMessage> resolved = resolveMessages(messages, options);
        ToolRegistry registry = buildToolRegistry(options);
        Map<String, Object> body = requestBuilder.buildMessagesBody(resolved, false, modelName,
                registry != null ? registry.definitions() : options != null ? options.getTools() : null,
                options != null ? options.getResponseFormat() : null);
        String jsonBody = requestBuilder.serializeBody(body);
        SynapseResponse response = executeWithRetry(jsonBody, false);

        if (registry != null && iteration < config.getMaxToolIterations()
                && response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            List<ChatMessage> continued = new ArrayList<>(resolved);
            continued.add(toAssistantMessage(response));
            for (ToolCall call : response.getToolCalls()) {
                String result = registry.invoke(call.getFunction(), call.getArguments());
                continued.add(ChatMessage.tool(call.getId(), call.getFunction(), result));
            }
            return sendChatWithToolLoop(continued, options, iteration + 1);
        }
        return response;
    }

    private ToolRegistry buildToolRegistry(RequestOptions options) {
        if (options == null || options.getToolInstances() == null || options.getToolInstances().isEmpty()) {
            return null;
        }
        return new ToolRegistry(objectMapper, options.getToolInstances());
    }

    @Override
    public <T> T sendPrompt(String prompt, Class<T> returnType, RequestOptions options) throws SynapseException {
        checkNotClosed();
        JsonNode schema = JsonSchemaGenerator.generateObjectSchema(returnType, objectMapper);
        try {
            String schemaJson = objectMapper.writeValueAsString(schema);
            RequestOptions effective = options != null ? options : RequestOptions.defaults();

            RequestOptions withSchema = new RequestOptions();
            withSchema.setModelName(effective.getModelName())
                    .setVariables(effective.getVariables())
                    .setToolInstances(effective.getToolInstances());

            SynapseResponse response;
            if (adapter.supportsJsonSchemaStructuredOutput()) {
                withSchema.setResponseFormat(ResponseFormat.jsonSchema(returnType.getSimpleName(), schemaJson));
                response = sendPrompt(prompt, withSchema);
            } else {
                withSchema.setResponseFormat(null);
                String injected = prompt + "\n\nRespond with a single JSON object matching this JSON Schema:\n"
                        + schemaJson;
                response = sendPrompt(injected, withSchema);
            }
            return objectMapper.readValue(response.getContent(), returnType);
        } catch (SynapseException e) {
            throw e;
        } catch (Exception e) {
            throw new SynapseException("Structured output failed: " + e.getMessage(), e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    private static ChatMessage toAssistantMessage(SynapseResponse response) {        ChatMessage message = new ChatMessage("assistant", response.getContent());
        message.setToolCalls(response.getToolCalls());
        return message;
    }

    @Override
    public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException {
        checkNotClosed();
        return sendChatAsync(List.of(ChatMessage.user(prompt)), options);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        checkNotClosed();
        return CompletableFuture.supplyAsync(() -> sendChat(messages, options), asyncExecutor);
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException {
        checkNotClosed();
        String modelName = resolveModel(options);
        String overriddenBody = requestBuilder.replaceModelInBody(requestBody, modelName);
        return executeWithRetry(overriddenBody, false);
    }

    @Override
    public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
        checkNotClosed();
        return streamChat(List.of(ChatMessage.user(prompt)), listener);
    }

    @Override
    public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true);
        String jsonBody = requestBuilder.serializeBody(body);
        return streamCompletion(jsonBody, listener);
    }

    @Override
    public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
        checkNotClosed();
        circuitBreaker.allowRequest();
        try {
            concurrencyLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SynapseException("Request interrupted while waiting for concurrency slot",
                    e, SynapseException.ExceptionType.CONFIG_ERROR);
        }
        try {
            if (rateLimiter != null) {
                rateLimiter.acquire();
            }
            CancellationToken token = new CancellationToken();
            CompletableFuture<SynapseResponse> future = new CompletableFuture<>();
            String correlationId = java.util.UUID.randomUUID().toString();

            asyncExecutor.submit(() -> {
                try {
                    String url = requestBuilder.buildUrl();
                    SynapseRequestContext reqCtx = buildRequestContext(url, requestBody, true);

                    notify(config.getRequestInterceptor(), SynapseRequestInterceptor::beforeRequest, reqCtx);

                    HttpRequest request = requestBuilder.buildPostRequest(reqCtx.getUrl(), reqCtx.getBody());
                    long startTime = System.currentTimeMillis();

                    SynapseResponse fullResponse = streamHandler.handleWithStreamListener(
                            request, listener, token, settings.enableLogging);
                    fullResponse.setCorrelationId(correlationId);
                    circuitBreaker.recordSuccess();
                    metricsCollector.recordSuccess(startTime);
                    future.complete(fullResponse);

                    SynapseResponseContext resCtx = new SynapseResponseContext(200, "", Map.of(),
                            System.currentTimeMillis() - startTime, settings.modelName, 0);
                    notify(config.getResponseInterceptor(), SynapseResponseInterceptor::afterResponse, resCtx);
                } catch (SynapseException e) {
                    circuitBreaker.recordFailure();
                    listener.onError(e);
                    future.completeExceptionally(e);
                } catch (Exception e) {
                    circuitBreaker.recordFailure();
                    SynapseException ex = new SynapseException("Streaming request failed", e,
                            SynapseException.ExceptionType.STREAMING_ERROR);
                    listener.onError(ex);
                    future.completeExceptionally(ex);
                } finally {
                    concurrencyLimiter.release();
                }
            });

            return new StreamHandle(token, future);
        } catch (Exception e) {
            concurrencyLimiter.release();
            throw e;
        }
    }

    @Override
    public Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true);
        String jsonBody = requestBuilder.serializeBody(body);
        return streamCompletionAsFlow(jsonBody);
    }

    @Override
    public Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(List.of(ChatMessage.user(prompt)), true);
        String jsonBody = requestBuilder.serializeBody(body);
        return streamCompletionAsFlow(jsonBody);
    }

    @Override
    public List<Model> getModelsList() throws SynapseException {
        checkNotClosed();
        String baseUrl = adapter.buildModelsUrl(config.getBaseUrl());

        HttpRequest request = requestBuilder.buildGetRequest(baseUrl);
        log.debug("[Synapse] Fetching models list from: {}", baseUrl);
        long start = System.currentTimeMillis();
        HttpResponse<String> response = httpClient.send(request);
        log.debug("[Synapse] Models list fetched in {}ms with status {}", System.currentTimeMillis() - start, response.statusCode());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            metricsCollector.recordFailure(start);
            throw new SynapseException(response.statusCode(), response.body());
        }

        metricsCollector.recordSuccess(start);
        return responseParser.parseModels(response.body());
    }

    private String resolveModel(RequestOptions options) {
        return options != null && options.getModelName() != null
                ? options.getModelName()
                : settings.modelName;
    }

    private List<ChatMessage> resolveMessages(List<ChatMessage> messages, RequestOptions options) {
        Map<String, Object> variables = options != null ? options.getVariables() : null;
        if (variables == null || variables.isEmpty()) return messages;
        List<ChatMessage> rendered = new ArrayList<>(messages.size());
        for (ChatMessage message : messages) {
            rendered.add(message.withVariables(variables));
        }
        return rendered;
    }

    private SynapseResponse executeWithRetry(String requestBody, boolean streaming) throws SynapseException {
        return retryHandler.executeWithRetry(() -> executeRequest(requestBody, streaming));
    }

    private SynapseResponse executeRequest(String requestBody, boolean streaming) throws SynapseException {
        circuitBreaker.allowRequest();
        try {
            concurrencyLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SynapseException("Request interrupted while waiting for concurrency slot",
                    e, SynapseException.ExceptionType.CONFIG_ERROR);
        }
        try {
            if (rateLimiter != null) {
                rateLimiter.acquire();
            }
            String url = requestBuilder.buildUrl();
            SynapseRequestContext reqCtx = buildRequestContext(url, requestBody, streaming);

            notify(config.getRequestInterceptor(), SynapseRequestInterceptor::beforeRequest, reqCtx);

            HttpRequest request = requestBuilder.buildPostRequest(reqCtx.getUrl(), reqCtx.getBody());
            log.debug("[Synapse] Request to: {}", url);
            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request);

            SynapseResponseContext resCtx = buildResponseContext(response, start);
            notify(config.getResponseInterceptor(), SynapseResponseInterceptor::beforeResponse, resCtx);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                circuitBreaker.recordFailure();
                SynapseException ex = new SynapseException(response.statusCode(), response.body());
                notifyError(reqCtx, resCtx, ex);
                metricsCollector.recordFailure(start);
                throw ex;
            }

            SynapseResponse synapseResponse = responseParser.parse(response.body());
            circuitBreaker.recordSuccess();
            metricsCollector.recordSuccess(start,
                    synapseResponse.getPromptTokens(), synapseResponse.getCompletionTokens());

            notifyComplete(reqCtx, resCtx);
            return synapseResponse;
        } finally {
            concurrencyLimiter.release();
        }
    }

    private Flow.Publisher<String> streamCompletionAsFlow(String requestBody) {
        FlowPublisher<String> publisher = new FlowPublisher<>();
        CancellationToken token = new CancellationToken();

        asyncExecutor.submit(() -> {
            try {
                String url = requestBuilder.buildUrl();
                HttpRequest request = requestBuilder.buildPostRequest(url, requestBody);
                streamHandler.handleAsFlow(request, publisher, token, settings.enableLogging);
            } catch (Exception e) {
                publisher.fail(e);
            }
        });

        return publisher;
    }

    private void checkNotClosed() throws SynapseException {
        if (closed) {
            throw new SynapseException("SynapseHub is closed",
                    SynapseException.ExceptionType.CONFIG_ERROR);
        }
    }

    private SynapseRequestContext buildRequestContext(String url, String body, boolean streaming) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new SynapseRequestContext(url, body, headers, streaming, settings.modelName);
    }

    private SynapseResponseContext buildResponseContext(HttpResponse<String> response, long startTime) {
        return new SynapseResponseContext(response.statusCode(),
                response.body(), Map.of(), System.currentTimeMillis() - startTime, settings.modelName, 0);
    }

    private void notifyError(SynapseRequestContext reqCtx, SynapseResponseContext resCtx, SynapseException ex) {
        notify(config.getRequestInterceptor(),
                (SynapseRequestInterceptor i, SynapseRequestContext ctx) -> i.onError(ctx, ex), reqCtx);
        notify(config.getResponseInterceptor(),
                (SynapseResponseInterceptor i, SynapseResponseContext ctx) -> i.onError(ctx, ex), resCtx);
    }

    private void notifyComplete(SynapseRequestContext reqCtx, SynapseResponseContext resCtx) {
        notify(config.getRequestInterceptor(), SynapseRequestInterceptor::afterRequest, reqCtx);
        notify(config.getResponseInterceptor(), SynapseResponseInterceptor::afterResponse, resCtx);
    }

    private <I, T> void notify(I interceptor, BiConsumer<I, T> callback, T target) {
        if (interceptor != null) callback.accept(interceptor, target);
    }


    public SynapseMetrics getMetrics() {
        return metrics;
    }

    /**
     * Rotates the API key for subsequent requests.
     *
     * <p>The shared {@link java.net.http.HttpClient} connection pool and async
     * executor are left untouched, so rotation is cheap and in-flight requests
     * keep the old credential.</p>
     *
     * @param apiKey the new API key; must not be blank
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the key is blank or the hub is closed
     */
    public SynapseHub updateApiKey(String apiKey) {
        requireOpen();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey must not be blank");
        }
        settings.apiKey = apiKey;
        log.info("[Synapse] API key rotated");
        return this;
    }

    /**
     * Switches the default model for requests that do not specify one.
     *
     * @param modelName the new default model; must not be blank
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the model is blank or the hub is closed
     */
    public SynapseHub updateDefaultModel(String modelName) {
        requireOpen();
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("modelName must not be blank");
        }
        settings.modelName = modelName;
        log.info("[Synapse] Default model updated to {}", modelName);
        return this;
    }

    /**
     * Changes the provider base URL for subsequent requests.
     *
     * @param baseUrl the new base URL; must not be blank
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the URL is blank or the hub is closed
     */
    public SynapseHub updateBaseUrl(String baseUrl) {
        requireOpen();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be blank");
        }
        settings.baseUrl = baseUrl;
        log.info("[Synapse] Base URL updated");
        return this;
    }

    /**
     * Changes the provider endpoint for subsequent requests.
     *
     * @param endpoint the new endpoint; must not be blank
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the endpoint is blank or the hub is closed
     */
    public SynapseHub updateEndpoint(String endpoint) {
        requireOpen();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint must not be blank");
        }
        settings.endpoint = endpoint;
        return this;
    }

    /**
     * Updates the per-request timeout applied to outgoing HTTP requests.
     *
     * @param requestTimeout the new timeout; must not be {@code null}
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the timeout is {@code null} or the hub is closed
     */
    public SynapseHub updateRequestTimeout(Duration requestTimeout) {
        requireOpen();
        if (requestTimeout == null) {
            throw new IllegalArgumentException("requestTimeout must not be null");
        }
        settings.requestTimeout = requestTimeout;
        log.info("[Synapse] Request timeout updated to {}", requestTimeout);
        return this;
    }

    /**
     * Updates the default temperature for subsequent requests.
     *
     * @param temperature the new temperature
     * @return this hub, for chaining
     */
    public SynapseHub updateTemperature(double temperature) {
        requireOpen();
        settings.temperature = temperature;
        return this;
    }

    /**
     * Updates the default max tokens for subsequent requests.
     *
     * @param maxTokens the new max tokens; must be positive
     * @return this hub, for chaining
     * @throws IllegalArgumentException if maxTokens is not positive or the hub is closed
     */
    public SynapseHub updateMaxTokens(int maxTokens) {
        requireOpen();
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be positive");
        }
        settings.maxTokens = maxTokens;
        return this;
    }

    /**
     * Switches authentication to a dynamic {@link TokenProvider}.
     *
     * @param tokenProvider the new token source; must not be {@code null}
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the provider is {@code null} or the hub is closed
     */
    public SynapseHub updateTokenProvider(TokenProvider tokenProvider) {
        requireOpen();
        if (tokenProvider == null) {
            throw new IllegalArgumentException("tokenProvider must not be null");
        }
        settings.tokenProvider = tokenProvider;
        log.info("[Synapse] Token provider updated");
        return this;
    }

    /**
     * Reconfigures the runtime-tunable settings from a new {@link SynapseConfig}.
     *
     * <p>Only the dynamic fields are applied (API key, token provider, base URL,
     * endpoint, default model, request timeout, temperature, max tokens, logging).
     * The HTTP client pool, async executor, circuit breaker, rate limiter,
     * interceptors, and metrics are preserved.</p>
     *
     * @param config the config whose dynamic fields should take effect
     * @return this hub, for chaining
     * @throws IllegalArgumentException if the config is invalid or the hub is closed
     */
    public SynapseHub reconfigure(SynapseConfig config) {
        requireOpen();
        try {
            config.validate();
        } catch (SynapseException e) {
            throw new IllegalArgumentException("Invalid SynapseConfig: " + e.getMessage());
        }
        settings.updateFrom(config);
        log.info("[Synapse] Hub reconfigured");
        return this;
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("SynapseHub is closed");
        }
    }

    @Override
    public void close() {
        closed = true;
        asyncExecutor.shutdownNow();
        log.info("[Synapse] Hub closed");
    }

    public SynapseConfig getConfig() {
        return config;
    }
}
