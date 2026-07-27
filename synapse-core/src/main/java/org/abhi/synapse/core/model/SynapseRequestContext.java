package org.abhi.synapse.core.model;

/**
 * Encapsulates the details of an outgoing HTTP request to an LLM provider.
 *
 * <p>{@code SynapseRequestContext} serves as an immutable-like container for all the data
 * needed to construct and send an HTTP request to an LLM API endpoint. It includes the
 * target URL, request body, HTTP headers, streaming flag, and model identifier. Headers
 * are defensively copied on construction and returned as immutable views to ensure
 * thread safety.</p>
 *
 * <p>This context is primarily used internally by the Synapse framework to pass request
 * details through the processing pipeline, including logging, interceptors, and retry logic.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SynapseRequestContext context = new SynapseRequestContext(
 *     "https://api.openai.com/v1/chat/completions",
 *     "{\"model\":\"gpt-4\",\"messages\":[...]}",
 *     Map.of("Authorization", "Bearer sk-..."),
 *     false,
 *     "gpt-4"
 * );
 *
 * String url = context.getUrl();
 * String body = context.getBody();
 * boolean isStreaming = context.isStreaming();
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseResponseContext
 */
public class SynapseRequestContext {

    private String url;
    private String body;
    private final java.util.Map<String, String> headers;
    private final boolean streaming;
    private final String model;

    /**
     * Constructs a new {@code SynapseRequestContext} with all required fields.
     *
     * <p>The provided headers map is defensively copied to prevent external modification.</p>
     *
     * @param url       the target URL for the LLM API endpoint
     * @param body      the JSON request body to send
     * @param headers   the HTTP headers to include in the request (defensively copied)
     * @param streaming {@code true} if the request uses server-sent events for streaming
     * @param model     the identifier of the LLM model being used
     * @since 1.0.0
     */
    public SynapseRequestContext(String url, String body, java.util.Map<String, String> headers,
                                 boolean streaming, String model) {
        this.url = url;
        this.body = body;
        this.headers = new java.util.HashMap<>(headers);
        this.streaming = streaming;
        this.model = model;
    }

    /**
     * Returns the target URL for the LLM API endpoint.
     *
     * @return the request URL
     * @since 1.0.0
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the target URL for the LLM API endpoint.
     *
     * @param url the request URL
     * @since 1.0.0
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the JSON request body.
     *
     * @return the request body string
     * @since 1.0.0
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the JSON request body.
     *
     * @param body the request body string
     * @since 1.0.0
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns an immutable copy of all HTTP headers.
     *
     * <p>The returned map is a defensive copy; modifications to it will not affect
     * the internal header storage. Use {@link #setHeader(String, String)},
     * {@link #addHeader(String, String)}, or {@link #removeHeader(String)} to modify headers.</p>
     *
     * @return an immutable map of header name-value pairs
     * @since 1.0.0
     */
    public java.util.Map<String, String> getHeaders() {
        return java.util.Map.copyOf(headers);
    }

    /**
     * Returns the value of a specific HTTP header by name.
     *
     * @param name the header name (case-sensitive)
     * @return the header value, or {@code null} if the header is not present
     * @since 1.0.0
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Sets the value of a specific HTTP header, creating it if it does not exist.
     *
     * @param name  the header name
     * @param value the header value
     * @since 1.0.0
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Adds or updates an HTTP header in this request context.
     *
     * <p>This method is functionally equivalent to {@link #setHeader(String, String)}.</p>
     *
     * @param name  the header name
     * @param value the header value
     * @since 1.0.0
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * Removes an HTTP header from this request context.
     *
     * @param name the header name to remove
     * @since 1.0.0
     */
    public void removeHeader(String name) {
        headers.remove(name);
    }

    /**
     * Returns whether this request uses server-sent events (SSE) for streaming.
     *
     * @return {@code true} if the request is a streaming request, {@code false} otherwise
     * @since 1.0.0
     */
    public boolean isStreaming() {
        return streaming;
    }

    /**
     * Returns the identifier of the LLM model being used for this request.
     *
     * @return the model identifier (e.g., {@code "gpt-4"}, {@code "claude-3-opus"})
     * @since 1.0.0
     */
    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        java.util.Map<String, String> maskedHeaders = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, String> e : headers.entrySet()) {
            String key = e.getKey();
            if (key.toLowerCase().contains("key") || key.equalsIgnoreCase("authorization")) {
                maskedHeaders.put(key, "***REDACTED***");
            } else {
                maskedHeaders.put(key, e.getValue());
            }
        }
        return "SynapseRequestContext{url='" + url + "', streaming=" + streaming
                + ", model='" + model + "', headers=" + maskedHeaders + "}";
    }
}
