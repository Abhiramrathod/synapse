package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.StreamHandle;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.CancellationToken;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseRequestContext;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.SynapseResponseContext;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.metrics.SynapseMetrics;
import org.abhi.synapse.metrics.SynapseMetricsCollector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SynapseHub implements ISynapseHub, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(SynapseHub.class);

    private final SynapseConfig config;
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
    private volatile boolean closed = false;

    public SynapseHub(SynapseConfig config) {
        this(config, new ObjectMapper());
    }

    public SynapseHub(SynapseConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.metrics = new SynapseMetrics();
        this.requestBuilder = new SynapseRequestBuilder(config, objectMapper);
        this.responseParser = new SynapseResponseParser(objectMapper);
        this.httpClient = new SynapseHttpClient(
                HttpClient.newBuilder().connectTimeout(config.getTimeout()).build());
        this.streamHandler = new SynapseStreamHandler(httpClient, objectMapper);
        this.retryHandler = new SynapseRetryHandler(config);
        this.metricsCollector = new SynapseMetricsCollector(metrics, config);
        this.circuitBreaker = new CircuitBreaker(
                config.getCircuitBreakerFailureThreshold(),
                config.getCircuitBreakerOpenDuration());
        this.concurrencyLimiter = new ConcurrencyLimiter(config.getMaxConcurrentRequests());
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "synapse-async-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });
        validateConfig();
        log.info("[Synapse] Hub initialized for model: {}", config.getModelName());
    }

    private void validateConfig() {
        try {
            config.validate();
        } catch (SynapseException e) {
            throw new IllegalArgumentException("Invalid SynapseConfig: " + e.getMessage());
        }
    }

    @Override
    public SynapseResponse sendPrompt(String prompt) throws SynapseException {
        checkNotClosed();
        return sendChat(List.of(ChatMessage.user(prompt)));
    }

    @Override
    public SynapseResponse sendPrompt(String prompt, String modelName) throws SynapseException {
        checkNotClosed();
        return sendChat(List.of(ChatMessage.user(prompt)), modelName);
    }

    @Override
    public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
        checkNotClosed();
        return sendChat(List.of(ChatMessage.user(prompt)), options);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt) throws SynapseException {
        checkNotClosed();
        return sendChatAsync(List.of(ChatMessage.user(prompt)));
    }

    @Override
    public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException {
        checkNotClosed();
        return sendChatAsync(List.of(ChatMessage.user(prompt)), options);
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, false);
        String jsonBody = requestBuilder.serializeBody(body);
        return executeWithRetry(jsonBody, false);
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages, String modelName) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, false, modelName);
        String jsonBody = requestBuilder.serializeBody(body);
        return executeWithRetry(jsonBody, false);
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        checkNotClosed();
        String modelName = options != null && options.getModelName() != null ? options.getModelName() : config.getModelName();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, false, modelName);
        String jsonBody = requestBuilder.serializeBody(body);
        return executeWithRetry(jsonBody, false);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages) throws SynapseException {
        checkNotClosed();
        return CompletableFuture.supplyAsync(() -> sendChat(messages), asyncExecutor);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        checkNotClosed();
        return CompletableFuture.supplyAsync(() -> sendChat(messages, options), asyncExecutor);
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody) throws SynapseException {
        checkNotClosed();
        return executeWithRetry(requestBody, false);
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody, String modelName) throws SynapseException {
        checkNotClosed();
        String overriddenBody = requestBuilder.replaceModelInBody(requestBody, modelName);
        return executeWithRetry(overriddenBody, false);
    }

    @Override
    public void streamPrompt(String prompt, java.util.function.Consumer<String> onChunk) throws SynapseException {
        checkNotClosed();
        streamChat(List.of(ChatMessage.user(prompt)), onChunk);
    }

    @Override
    public void streamPrompt(String prompt, java.util.function.Consumer<String> onChunk, String modelName) throws SynapseException {
        checkNotClosed();
        streamChat(List.of(ChatMessage.user(prompt)), onChunk, modelName);
    }

    @Override
    public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
        checkNotClosed();
        return streamChat(List.of(ChatMessage.user(prompt)), listener);
    }

    @Override
    public void streamChat(List<ChatMessage> messages, java.util.function.Consumer<String> onChunk) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true);
        String jsonBody = requestBuilder.serializeBody(body);
        streamCompletion(jsonBody, onChunk);
    }

    @Override
    public void streamChat(List<ChatMessage> messages, java.util.function.Consumer<String> onChunk, String modelName) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true, modelName);
        String jsonBody = requestBuilder.serializeBody(body);
        streamCompletion(jsonBody, onChunk);
    }

    @Override
    public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true);
        String jsonBody = requestBuilder.serializeBody(body);
        return streamCompletion(jsonBody, listener);
    }

    @Override
    public void streamCompletion(String requestBody, java.util.function.Consumer<String> onChunk) throws SynapseException {
        streamCompletion(requestBody, StreamListener.of(onChunk));
    }

    @Override
    public void streamCompletion(String requestBody, java.util.function.Consumer<String> onChunk, String modelName) throws SynapseException {
        String overriddenBody = requestBuilder.replaceModelInBody(requestBody, modelName);
        streamCompletion(overriddenBody, StreamListener.of(onChunk));
    }

    @Override
    public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
        checkNotClosed();
        try {
            circuitBreaker.allowRequest();
        } catch (SynapseException e) {
            throw e;
        }
        try {
            concurrencyLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SynapseException("Request interrupted while waiting for concurrency slot",
                    e, SynapseException.ExceptionType.CONFIG_ERROR);
        }
        try {
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
                            request, listener, token, config.isEnableLogging());
                    fullResponse.setCorrelationId(correlationId);
                    circuitBreaker.recordSuccess();
                    metricsCollector.recordSuccess(startTime);
                    future.complete(fullResponse);

                    SynapseResponseContext resCtx = new SynapseResponseContext(200, "", Map.of(),
                            System.currentTimeMillis() - startTime, config.getModelName(), 0);
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
    public Flow.Publisher<String> streamCompletionAsFlow(String requestBody) throws SynapseException {
        checkNotClosed();
        FlowPublisher<String> publisher = new FlowPublisher<>();
        CancellationToken token = new CancellationToken();

        asyncExecutor.submit(() -> {
            try {
                String url = requestBuilder.buildUrl();
                HttpRequest request = requestBuilder.buildPostRequest(url, requestBody);
                streamHandler.handleAsFlow(request, publisher, token, config.isEnableLogging());
            } catch (Exception e) {
                publisher.fail(e);
            }
        });

        return publisher;
    }

    @Override
    public Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(List.of(ChatMessage.user(prompt)), true);
        String jsonBody = requestBuilder.serializeBody(body);
        return streamCompletionAsFlow(jsonBody);
    }

    @Override
    public List<Model> getModelsList() {
        checkNotClosed();
        String cleanUrl = config.getBaseUrl().replaceAll("/+$", "");
        String baseUrl = cleanUrl.endsWith("/v1") ? cleanUrl + "/models" : cleanUrl + "/v1/models";

        HttpRequest request = requestBuilder.buildGetRequest(baseUrl);
        TimedResult<HttpResponse<String>> timed = executeWithTiming("Fetching models list from: " + baseUrl,
                () -> httpClient.send(request));
        HttpResponse<String> response = timed.value();
        long latencyMs = System.currentTimeMillis() - timed.startTime();
        log.debug("[Synapse] Models list fetched in {}ms with status {}", latencyMs, response.statusCode());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            metricsCollector.recordFailure(timed.startTime());
            throw new SynapseException(response.statusCode(), response.body());
        }

        metricsCollector.recordSuccess(timed.startTime());
        return responseParser.parseModels(response.body());
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
            String url = requestBuilder.buildUrl();
            SynapseRequestContext reqCtx = buildRequestContext(url, requestBody, streaming);

            notify(config.getRequestInterceptor(), SynapseRequestInterceptor::beforeRequest, reqCtx);

            HttpRequest request = requestBuilder.buildPostRequest(reqCtx.getUrl(), reqCtx.getBody());
            TimedResult<HttpResponse<String>> timed = executeWithTiming("Request to: " + url,
                    () -> httpClient.send(request));
            HttpResponse<String> response = timed.value();

            SynapseResponseContext resCtx = buildResponseContext(response, timed.startTime());
            notify(config.getResponseInterceptor(), SynapseResponseInterceptor::beforeResponse, resCtx);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                circuitBreaker.recordFailure();
                SynapseException ex = new SynapseException(response.statusCode(), response.body());
                notifyError(reqCtx, resCtx, ex);
                metricsCollector.recordFailure(timed.startTime());
                throw ex;
            }

            SynapseResponse synapseResponse = responseParser.parse(response.body());
            circuitBreaker.recordSuccess();
            metricsCollector.recordSuccess(timed.startTime(),
                    synapseResponse.getPromptTokens(), synapseResponse.getCompletionTokens());

            notifyComplete(reqCtx, resCtx);
            return synapseResponse;
        } finally {
            concurrencyLimiter.release();
        }
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
        return new SynapseRequestContext(url, body, headers, streaming, config.getModelName());
    }

    private SynapseResponseContext buildResponseContext(HttpResponse<String> response, long startTime) {
        return new SynapseResponseContext(response.statusCode(),
                response.body(), Map.of(), System.currentTimeMillis() - startTime, config.getModelName(), 0);
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

    private record TimedResult<T>(T value, long startTime) {}

    private <T> TimedResult<T> executeWithTiming(String logMessage, Supplier<T> action) {
        log.debug("[Synapse] {}", logMessage);
        long start = System.currentTimeMillis();
        return new TimedResult<>(action.get(), start);
    }

    public SynapseMetrics getMetrics() {
        return metrics;
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
