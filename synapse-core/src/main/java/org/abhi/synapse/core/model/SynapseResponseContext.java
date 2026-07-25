package org.abhi.synapse.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the full details of an HTTP response received from an LLM provider.
 *
 * <p>{@code SynapseResponseContext} captures the complete HTTP response including the status
 * code, body, headers, measured latency, model identifier, and token consumption. It provides
 * convenience methods for determining whether the response indicates success or a retryable
 * failure.</p>
 *
 * <p>This context is primarily used internally by the Synapse framework for logging,
 * metrics collection, and retry decision-making. It may also be exposed to callers
 * for advanced diagnostics and monitoring.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SynapseResponseContext ctx = responseContext;
 * if (ctx.isSuccess()) {
 *     log.info("Model: {}, Latency: {}ms, Tokens: {}",
 *         ctx.getModel(), ctx.getLatencyMs(), ctx.getTokensUsed());
 * } else if (ctx.isRetryable()) {
 *     log.warn("Retryable error ({}), will retry", ctx.getStatusCode());
 * }
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseRequestContext
 * @see SynapseResponse
 */
public class SynapseResponseContext {

    private int statusCode;
    private String body;
    private final Map<String, List<String>> headers;
    private final long latencyMs;
    private final String model;
    private int tokensUsed;

    /**
     * Constructs a new {@code SynapseResponseContext} with all response details.
     *
     * <p>The provided headers map is defensively copied to prevent external modification.</p>
     *
     * @param statusCode the HTTP status code from the LLM provider response
     * @param body       the raw response body
     * @param headers    the HTTP response headers (defensively copied)
     * @param latencyMs  the total round-trip latency in milliseconds
     * @param model      the identifier of the model that generated the response
     * @param tokensUsed the total number of tokens consumed by the request
     * @since 1.0.0
     */
    public SynapseResponseContext(int statusCode, String body, Map<String, List<String>> headers,
                                   long latencyMs, String model, int tokensUsed) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>(headers);
        this.latencyMs = latencyMs;
        this.model = model;
        this.tokensUsed = tokensUsed;
    }

    /**
     * Returns the HTTP status code from the LLM provider response.
     *
     * @return the HTTP status code (e.g., 200, 429, 500)
     * @since 1.0.0
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the HTTP status code from the LLM provider response.
     *
     * @param statusCode the HTTP status code
     * @since 1.0.0
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the raw response body from the LLM provider.
     *
     * @return the response body string
     * @since 1.0.0
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the raw response body from the LLM provider.
     *
     * @param body the response body string
     * @since 1.0.0
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns an immutable copy of all HTTP response headers.
     *
     * <p>The returned map is a defensive copy; modifications to it will not affect
     * the internal header storage.</p>
     *
     * @return an immutable map of header name to list of header values
     * @since 1.0.0
     */
    public Map<String, List<String>> getHeaders() {
        return Map.copyOf(headers);
    }

    /**
     * Returns the total round-trip latency of the request in milliseconds.
     *
     * @return the latency in milliseconds
     * @since 1.0.0
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * Returns the identifier of the model that generated the response.
     *
     * @return the model identifier (e.g., {@code "gpt-4"}, {@code "claude-3-opus"})
     * @since 1.0.0
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the total number of tokens consumed by the request.
     *
     * @return the total token count (prompt + completion)
     * @since 1.0.0
     */
    public int getTokensUsed() {
        return tokensUsed;
    }

    /**
     * Determines whether the response indicates a successful HTTP operation.
     *
     * <p>Returns {@code true} if the status code is in the 2xx range (200-299).</p>
     *
     * @return {@code true} if the status code indicates success, {@code false} otherwise
     * @since 1.0.0
     */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Determines whether the response indicates a transient failure that can be retried.
     *
     * <p>Returns {@code true} for HTTP 429 (rate limit) and 5xx (server error) status codes.</p>
     *
     * @return {@code true} if the request can be retried, {@code false} otherwise
     * @since 1.0.0
     */
    public boolean isRetryable() {
        return statusCode == 429 || statusCode >= 500;
    }
}
