package org.abhi.synapse.core.model;

/**
 * An immutable summary of metrics collected from an LLM interaction.
 *
 * <p>{@code SynapseMetricsSummary} provides a snapshot of key performance indicators
 * for a single LLM request, including the model used, latency, token consumption, and
 * whether the request succeeded. All fields are final, ensuring thread safety and
 * immutability once constructed.</p>
 *
 * <p>This model is typically used for logging, monitoring, and aggregation of LLM
 * usage statistics across multiple requests.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SynapseMetricsSummary metrics = new SynapseMetricsSummary(
 *     "gpt-4", 1250L, 150, 320, true
 * );
 *
 * log.info("Model: {}, Latency: {}ms, Total tokens: {}, Success: {}",
 *     metrics.getModel(),
 *     metrics.getLatencyMs(),
 *     metrics.getTotalTokens(),
 *     metrics.isSuccess()
 * );
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseResponse
 * @see SynapseResponseContext
 */
public class SynapseMetricsSummary {

    private final String model;
    private final String provider;
    private final long latencyMs;
    private final int promptTokens;
    private final int completionTokens;
    private final boolean success;

    public SynapseMetricsSummary(String model, long latencyMs, int promptTokens,
                                  int completionTokens, boolean success) {
        this(model, null, latencyMs, promptTokens, completionTokens, success);
    }

    public SynapseMetricsSummary(String model, String provider, long latencyMs, int promptTokens,
                                  int completionTokens, boolean success) {
        this.model = model;
        this.provider = provider;
        this.latencyMs = latencyMs;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.success = success;
    }

    /**
     * Returns the identifier of the model used for the request.
     *
     * @return the model identifier (e.g., {@code "gpt-4"}, {@code "claude-3-opus"})
     * @since 1.0.0
     */
    public String getModel() {
        return model;
    }

    public String getProvider() {
        return provider;
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
     * Returns the number of tokens consumed by the input prompt.
     *
     * @return the prompt token count
     * @since 1.0.0
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * Returns the number of tokens generated in the response.
     *
     * @return the completion token count
     * @since 1.0.0
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * Returns the total number of tokens consumed by the request (prompt + completion).
     *
     * @return the sum of prompt and completion tokens
     * @since 1.0.0
     */
    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    /**
     * Returns whether the request completed successfully.
     *
     * @return {@code true} if the request succeeded, {@code false} otherwise
     * @since 1.0.0
     */
    public boolean isSuccess() {
        return success;
    }
}
