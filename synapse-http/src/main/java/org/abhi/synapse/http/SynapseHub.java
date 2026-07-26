package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseRequestContext;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.SynapseResponseContext;
import org.abhi.synapse.interceptors.SynapseRequestInterceptor;
import org.abhi.synapse.interceptors.SynapseResponseInterceptor;
import org.abhi.synapse.metrics.SynapseMetrics;
import org.abhi.synapse.metrics.SynapseMetricsCollector;

import com.fasterxml.jackson.databind.JsonNode;
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

/**
 * Primary implementation of {@link ISynapseHub} that provides HTTP-based communication
 * with LLM API endpoints. This class serves as the main entry point for all
 * synchronous and streaming interactions with language models.
 *
 * <p>{@code SynapseHub} orchestrates the full request lifecycle including request
 * construction, retry handling, response parsing, metrics collection, and interceptor
 * invocation. It is designed to be instantiated once and reused across multiple requests.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SynapseConfig config = SynapseConfig.builder()
 *     .baseUrl("https://api.openai.com")
 *     .endpoint("/v1/chat/completions")
 *     .apiKey("sk-...")
 *     .modelName("gpt-4")
 *     .build();
 *
 * try (SynapseHub hub = new SynapseHub(config)) {
 *     SynapseResponse response = hub.sendPrompt("Hello, world!");
 *     System.out.println(response.getContent());
 * }
 * }</pre>
 *
 * <p>This class implements {@link AutoCloseable} and should be closed when no longer needed
 * to release associated resources. Once closed, all subsequent API calls will throw
 * {@link SynapseException}.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see ISynapseHub
 * @see SynapseConfig
 * @see SynapseResponse
 */
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
    private final ObjectMapper objectMapper;
    private volatile boolean closed = false;

    /**
     * Constructs a new {@code SynapseHub} with the specified configuration and a
     * default {@link ObjectMapper} instance.
     *
     * @param config the {@link SynapseConfig} containing connection and model settings;
     *               must not be {@code null} and must pass validation
     * @throws IllegalArgumentException if the provided configuration is invalid
     * @since 1.0.0
     */
    public SynapseHub(SynapseConfig config) {
        this(config, new ObjectMapper());
    }

    /**
     * Constructs a new {@code SynapseHub} with the specified configuration and
     * a custom {@link ObjectMapper} for JSON serialization/deserialization.
     *
     * <p>This constructor allows customization of the JSON processing behavior,
     * which can be useful for advanced serialization configurations or testing.</p>
     *
     * @param config     the {@link SynapseConfig} containing connection and model settings;
     *                   must not be {@code null} and must pass validation
     * @param objectMapper the {@link ObjectMapper} to use for JSON processing;
     *                     must not be {@code null}
     * @throws IllegalArgumentException if the provided configuration is invalid
     * @since 1.0.0
     */
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
        validateConfig();
    }

    /**
     * Validates the provided {@link SynapseConfig} by delegating to its own validation logic.
     * This is called during construction to fail fast on invalid configurations.
     *
     * @throws IllegalArgumentException if the configuration fails validation
     * @since 1.0.0
     */
    private void validateConfig() {
        try {
            config.validate();
        } catch (SynapseException e) {
            throw new IllegalArgumentException("Invalid SynapseConfig: " + e.getMessage());
        }
    }

    /**
     * Sends a single prompt to the LLM and returns the complete response.
     *
     * <p>This is a convenience method that wraps the prompt in a {@link ChatMessage} and
     * delegates to {@link #sendChat(List)}. The prompt is sent as a single user message.</p>
     *
     * @param prompt the text prompt to send to the model; must not be {@code null} or empty
     * @return the {@link SynapseResponse} containing the model's response and usage metadata
     * @throws SynapseException if the request fails, is retried and exhausted,
     *                          or if the hub has been closed
     * @since 1.0.0
     */
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

    /**
     * Sends a list of chat messages to the LLM and returns the complete response.
     *
     * <p>The messages are serialized into an OpenAI-compatible request body and sent
     * to the configured endpoint. Retry logic is applied if the request fails with
     * a retryable error.</p>
     *
     * @param messages the list of {@link ChatMessage} objects representing the conversation;
     *                 must not be {@code null} or empty
     * @return the {@link SynapseResponse} containing the model's response and usage metadata
     * @throws SynapseException if the request fails, is retried and exhausted,
     *                          or if the hub has been closed
     * @since 1.0.0
     */
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

    /**
     * Sends a raw JSON request body directly to the LLM chat completion endpoint
     * and returns the parsed response.
     *
     * <p>This method provides low-level access for advanced use cases where the caller
     * needs full control over the request body format. The body must be a valid JSON
     * string conforming to the target API's chat completion schema.</p>
     *
     * @param requestBody the raw JSON string to send as the request body;
     *                    must not be {@code null}
     * @return the {@link SynapseResponse} containing the model's response and usage metadata
     * @throws SynapseException if the request fails, is retried and exhausted,
     *                          or if the hub has been closed
     * @since 1.0.0
     */
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

    /**
     * Sends a single prompt to the LLM and streams the response chunks via the
     * provided callback.
     *
     * <p>This is a convenience method that wraps the prompt in a {@link ChatMessage} and
     * delegates to {@link #streamChat(List, Consumer)}. Each content chunk is delivered
     * to the {@code onChunk} callback as it arrives from the server.</p>
     *
     * @param prompt  the text prompt to send to the model; must not be {@code null} or empty
     * @param onChunk the {@link Consumer} callback that receives each text chunk from the
     *                streaming response; must not be {@code null}
     * @throws SynapseException if the streaming request fails,
     *                          or if the hub has been closed
     * @since 1.0.0
     */
    @Override
    public void streamPrompt(String prompt, Consumer<String> onChunk) throws SynapseException {
        checkNotClosed();
        streamChat(List.of(ChatMessage.user(prompt)), onChunk);
    }

    @Override
    public void streamPrompt(String prompt, Consumer<String> onChunk, String modelName) throws SynapseException {
        checkNotClosed();
        streamChat(List.of(ChatMessage.user(prompt)), onChunk, modelName);
    }

    /**
     * Sends a list of chat messages to the LLM and streams the response chunks via the
     * provided callback.
     *
     * <p>The messages are serialized into an OpenAI-compatible streaming request body.
     * Each content chunk from the server-side events (SSE) stream is delivered to the
     * {@code onChunk} callback as it arrives.</p>
     *
     * @param messages the list of {@link ChatMessage} objects representing the conversation;
     *                 must not be {@code null} or empty
     * @param onChunk the {@link Consumer} callback that receives each text chunk from the
     *                streaming response; must not be {@code null}
     * @throws SynapseException if the streaming request fails,
     *                          or if the hub has been closed
     * @since 1.0.0
     */
    @Override
    public void streamChat(List<ChatMessage> messages, Consumer<String> onChunk)
            throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true);
        String jsonBody = requestBuilder.serializeBody(body);
        streamCompletion(jsonBody, onChunk);
    }

    @Override
    public void streamChat(List<ChatMessage> messages, Consumer<String> onChunk, String modelName) throws SynapseException {
        checkNotClosed();
        Map<String, Object> body = requestBuilder.buildMessagesBody(messages, true, modelName);
        String jsonBody = requestBuilder.serializeBody(body);
        streamCompletion(jsonBody, onChunk);
    }

    /**
     * Sends a raw JSON request body to the LLM streaming chat completion endpoint
     * and delivers response chunks via the provided callback.
     *
     * <p>This method provides low-level access for advanced use cases where the caller
     * needs full control over the streaming request body format. The body must be a valid
     * JSON string conforming to the target API's streaming chat completion schema.</p>
     *
     * <p>The method handles the full streaming lifecycle including request interceptor
     * invocation, SSE stream processing, metrics recording, and response interceptor
     * notification.</p>
     *
     * @param requestBody the raw JSON string to send as the request body;
     *                    must not be {@code null}
     * @param onChunk     the {@link Consumer} callback that receives each text chunk from the
     *                    streaming response; must not be {@code null}
     * @throws SynapseException if the streaming request fails,
     *                          or if the hub has been closed
     * @since 1.0.0
     */
    @Override
    public void streamCompletion(String requestBody, Consumer<String> onChunk)
            throws SynapseException {
        checkNotClosed();
        String url = requestBuilder.buildUrl();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        SynapseRequestContext requestContext = new SynapseRequestContext(url, requestBody, headers,
                true, config.getModelName());

        SynapseRequestInterceptor requestInterceptor = config.getRequestInterceptor();
        if (requestInterceptor != null) {
            requestInterceptor.beforeRequest(requestContext);
        }

        HttpRequest request = requestBuilder.buildPostRequest(
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

    @Override
    public void streamCompletion(String requestBody, Consumer<String> onChunk, String modelName) throws SynapseException {
        checkNotClosed();
        String overriddenBody = requestBuilder.replaceModelInBody(requestBody, modelName);
        streamCompletion(overriddenBody, onChunk);
    }

    @Override
    public List<Model> getModelsList() throws SynapseException {
        checkNotClosed();

        String cleanUrl = config.getBaseUrl().replaceAll("/+$", "");
        String baseUrl = cleanUrl.endsWith("/v1") ? cleanUrl + "/models" : cleanUrl + "/v1/models";

        log(Level.FINE, "Fetching models list from: " + baseUrl);
        long startTime = System.currentTimeMillis();

        HttpRequest request = requestBuilder.buildGetRequest(baseUrl);
        HttpResponse<String> response = httpClient.send(request);

        long latencyMs = System.currentTimeMillis() - startTime;
        log(Level.FINE, "Models list fetched in " + latencyMs + "ms with status " + response.statusCode());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            metricsCollector.recordFailure(startTime);
            throw new SynapseException(response.statusCode(), response.body());
        }

        metricsCollector.recordSuccess(startTime);
        return parseModels(response.body());
    }


    private List<Model> parseModels(String responseBody) throws SynapseException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.path("data");
            List<Model> models = new java.util.ArrayList<>();
            for (JsonNode node : data) {
                Model model = new Model();
                model.setId(node.path("id").asText(null));
                model.setObject(node.path("object").asText(null));
                model.setCreated(node.path("created").asLong(0));
                model.setOwnedBy(node.path("owned_by").asText(null));
                models.add(model);
            }
            return models;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse models response", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }
    /**
     * Executes the given request body through the retry handler with the specified streaming mode.
     *
     * @param requestBody the JSON request body to send
     * @param streaming   {@code true} if the request is a streaming request;
     *                    {@code false} otherwise
     * @return the {@link SynapseResponse} containing the model's response
     * @throws SynapseException if all retry attempts are exhausted or the request is non-retryable
     * @since 1.0.0
     */
    private SynapseResponse executeWithRetry(String requestBody, boolean streaming)
            throws SynapseException {
        return retryHandler.executeWithRetry(() -> executeRequest(requestBody, streaming));
    }

    /**
     * Executes a single HTTP request to the LLM endpoint, including request interceptor
     * invocation, response parsing, metrics recording, and response interceptor notification.
     *
     * @param requestBody the JSON request body to send
     * @param streaming   {@code true} if the request is a streaming request;
     *                    {@code false} otherwise
     * @return the {@link SynapseResponse} containing the model's response and usage metadata
     * @throws SynapseException if the HTTP request fails, returns a non-2xx status,
     *                          or if response parsing fails
     * @since 1.0.0
     */
    private SynapseResponse executeRequest(String requestBody, boolean streaming)
            throws SynapseException {
        String url = requestBuilder.buildUrl();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        SynapseRequestContext requestContext = new SynapseRequestContext(url, requestBody, headers,
                streaming, config.getModelName());

        SynapseRequestInterceptor requestInterceptor = config.getRequestInterceptor();
        if (requestInterceptor != null) {
            requestInterceptor.beforeRequest(requestContext);
        }

        HttpRequest request = requestBuilder.buildPostRequest(
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

    /**
     * Checks that this {@code SynapseHub} instance has not been closed.
     * If it has been closed, a {@link SynapseException} with type
     * {@link SynapseException.ExceptionType#CONFIG_ERROR} is thrown.
     *
     * @throws SynapseException if this hub has been closed via {@link #close()}
     * @since 1.0.0
     */
    private void checkNotClosed() throws SynapseException {
        if (closed) {
            throw new SynapseException("SynapseHub is closed",
                    SynapseException.ExceptionType.CONFIG_ERROR);
        }
    }

    /**
     * Logs a message at the specified level if logging is enabled in the configuration.
     * Messages are prefixed with {@code [Synapse]} for easy identification.
     *
     * @param level   the {@link Level} at which to log the message
     * @param message the message to log
     * @since 1.0.0
     */
    private void log(Level level, String message) {
        if (config.isEnableLogging()) {
            LOGGER.log(level, "[Synapse] " + message);
        }
    }

    /**
     * Returns the {@link SynapseMetrics} instance associated with this hub,
     * providing access to request metrics such as success/failure counts,
     * latency, and token usage.
     *
     * @return the {@link SynapseMetrics} instance; never {@code null}
     * @since 1.0.0
     */
    public SynapseMetrics getMetrics() {
        return metrics;
    }

    /**
     * Closes this {@code SynapseHub} instance, marking it as no longer usable.
     *
     * <p>After calling this method, all subsequent attempts to send requests
     * will throw {@link SynapseException}. This method is idempotent and
     * safe to call multiple times.</p>
     *
     * @since 1.0.0
     */
    @Override
    public void close() {
        closed = true;
        log(Level.FINE, "SynapseHub closed");
    }

    /**
     * Returns the {@link SynapseConfig} used to initialize this hub.
     *
     * @return the configuration; never {@code null}
     * @since 1.0.0
     */
    public SynapseConfig getConfig() {
        return config;
    }
}
