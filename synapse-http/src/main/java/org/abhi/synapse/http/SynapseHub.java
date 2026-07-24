package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.SynapseRequestContext;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.SynapseResponseContext;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.metrics.SynapseMetrics;
import org.abhi.synapse.metrics.SynapseMetricsCollector;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Consumer;

public class SynapseHub implements ISynapseHub, AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(SynapseHub.class.getName());

    private final SynapseConfig config;
    private final SynapseRequestBuilder requestBuilder;
    private final SynapseResponseParser responseParser;
    private final SynapseHttpClient httpClient;
    private final SynapseStreamHandler streamHandler;
    private final SynapseRetryHandler retryHandler;
    private final SynapseMetricsCollector metricsCollector;
    private final SynapseMetrics metrics;
    private volatile boolean closed = false;

    public SynapseHub(SynapseConfig config) {
        this(config, new ObjectMapper());
    }

    public SynapseHub(SynapseConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.metrics = new SynapseMetrics();
        this.requestBuilder = new SynapseRequestBuilder(config, objectMapper);
        this.responseParser = new SynapseResponseParser(objectMapper);
        this.httpClient = new SynapseHttpClient(
                HttpClient.newBuilder().connectTimeout(config.getTimeout()).build());
        this.streamHandler = new SynapseStreamHandler(httpClient, objectMapper);
        this.retryHandler = new SynapseRetryHandler(config);
        this.metricsCollector = new SynapseMetricsCollector(metrics, config);
        validateConfig();
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
    public SynapseResponse sendChat(List<ChatMessage> messages) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, false);
        String jsonBody = requestBuilder.serializeBody(body);
        return executeWithRetry(jsonBody, false);
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody) throws SynapseException {
        checkNotClosed();
        return executeWithRetry(requestBody, false);
    }

    @Override
    public void streamPrompt(String prompt, Consumer<String> onChunk) throws SynapseException {
        checkNotClosed();
        streamChat(List.of(ChatMessage.user(prompt)), onChunk);
    }

    @Override
    public void streamChat(List<ChatMessage> messages, Consumer<String> onChunk)
            throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true);
        String jsonBody = requestBuilder.serializeBody(body);
        streamCompletion(jsonBody, onChunk);
    }

    @Override
    public void streamCompletion(String requestBody, Consumer<String> onChunk)
            throws SynapseException {
        checkNotClosed();
        String url = requestBuilder.buildUrl();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getApiKey());

        SynapseRequestContext requestContext = new SynapseRequestContext(url, requestBody, headers,
                true, config.getModelName());

        SynapseRequestInterceptor requestInterceptor = config.getRequestInterceptor();
        if (requestInterceptor != null) {
            requestInterceptor.beforeRequest(requestContext);
        }

        HttpRequest request = requestBuilder.buildRequest(
                requestContext.getUrl(), requestContext.getBody());

        log(Level.FINE, "Streaming request to: " + url);
        long startTime = System.currentTimeMillis();

        try {
            streamHandler.handle(request, onChunk, config.isEnableLogging());
            metricsCollector.recordSuccess(startTime);

            SynapseResponseContext responseContext = new SynapseResponseContext(200, "", Map.of(),
                    System.currentTimeMillis() - startTime, config.getModelName(), 0);
            SynapseResponseInterceptor responseInterceptor = config.getResponseInterceptor();
            if (responseInterceptor != null) {
                responseInterceptor.afterResponse(responseContext);
            }
        } catch (SynapseException e) {
            if (requestInterceptor != null) {
                requestInterceptor.onError(requestContext, e);
            }
            throw e;
        } catch (Exception e) {
            throw metricsCollector.recordFailureAndThrow(startTime,
                    "Streaming request failed", e, SynapseException.ExceptionType.STREAMING_ERROR);
        }
    }

    private SynapseResponse executeWithRetry(String requestBody, boolean streaming)
            throws SynapseException {
        return retryHandler.executeWithRetry(() -> executeRequest(requestBody, streaming));
    }

    private SynapseResponse executeRequest(String requestBody, boolean streaming)
            throws SynapseException {
        String url = requestBuilder.buildUrl();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + config.getApiKey());

        SynapseRequestContext requestContext = new SynapseRequestContext(url, requestBody, headers,
                streaming, config.getModelName());

        SynapseRequestInterceptor requestInterceptor = config.getRequestInterceptor();
        if (requestInterceptor != null) {
            requestInterceptor.beforeRequest(requestContext);
        }

        HttpRequest request = requestBuilder.buildRequest(
                requestContext.getUrl(), requestContext.getBody());

        log(Level.FINE, "Request to: " + url);
        long startTime = System.currentTimeMillis();

        HttpResponse<String> response = httpClient.send(request);

        long latencyMs = System.currentTimeMillis() - startTime;

        SynapseResponseContext responseContext = new SynapseResponseContext(response.statusCode(),
                response.body(), Map.of(), latencyMs, config.getModelName(), 0);

        SynapseResponseInterceptor responseInterceptor = config.getResponseInterceptor();
        if (responseInterceptor != null) {
            responseInterceptor.beforeResponse(responseContext);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            if (requestInterceptor != null) {
                requestInterceptor.onError(requestContext,
                        new SynapseException(response.statusCode(), response.body()));
            }
            if (responseInterceptor != null) {
                responseInterceptor.onError(responseContext,
                        new SynapseException(response.statusCode(), response.body()));
            }
            metricsCollector.recordFailure(startTime);
            throw new SynapseException(response.statusCode(), response.body());
        }

        SynapseResponse synapseResponse = responseParser.parse(response.body());
        metricsCollector.recordSuccess(startTime,
                synapseResponse.getPromptTokens(), synapseResponse.getCompletionTokens());

        if (requestInterceptor != null) {
            requestInterceptor.afterRequest(requestContext);
        }
        if (responseInterceptor != null) {
            responseInterceptor.afterResponse(responseContext);
        }

        return synapseResponse;
    }

    private void checkNotClosed() throws SynapseException {
        if (closed) {
            throw new SynapseException("SynapseHub is closed",
                    SynapseException.ExceptionType.CONFIG_ERROR);
        }
    }

    private void log(Level level, String message) {
        if (config.isEnableLogging()) {
            LOGGER.log(level, "[Synapse] " + message);
        }
    }

    public SynapseMetrics getMetrics() {
        return metrics;
    }

    @Override
    public void close() {
        closed = true;
        log(Level.FINE, "SynapseHub closed");
    }

    public SynapseConfig getConfig() {
        return config;
    }
}
