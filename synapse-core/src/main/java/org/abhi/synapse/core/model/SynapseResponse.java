package org.abhi.synapse.core.model;

import java.util.List;

/**
 * Represents the response returned by an LLM after a synchronous request.
 *
 * <p>{@code SynapseResponse} encapsulates the generated content, metadata about the model
 * used, token consumption statistics, and the reason the model stopped generating. This
 * object is returned by all synchronous methods on {@link org.abhi.synapse.core.ISynapseHub}.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SynapseResponse response = hub.sendPrompt("What is Java?");
 * String answer = response.getContent();
 * String model = response.getModel();
 * int totalTokens = response.getPromptTokens() + response.getCompletionTokens();
 *
 * System.out.println("Model: " + model);
 * System.out.println("Answer: " + answer);
 * System.out.println("Tokens used: " + totalTokens);
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see org.abhi.synapse.core.ISynapseHub#sendPrompt(String)
 * @see org.abhi.synapse.core.ISynapseHub#sendChat(java.util.List)
 */
public class SynapseResponse {

    private String content;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private String finishReason;
    private List<ToolCall> toolCalls;
    private String responseFormat;
    private String correlationId;
    private String provider;

    /**
     * Default no-argument constructor for {@code SynapseResponse}.
     *
     * <p>Primarily intended for use with JSON serialization/deserialization frameworks.</p>
     *
     * @since 1.0.0
     */
    public SynapseResponse() {
    }

    /**
     * Constructs a new {@code SynapseResponse} with all fields specified.
     *
     * @param content          the generated text content from the LLM
     * @param model            the identifier of the model that generated the response
     * @param promptTokens     the number of tokens consumed by the input prompt
     * @param completionTokens the number of tokens generated in the response
     * @param finishReason     the reason the model stopped generating (e.g., {@code "stop"}, {@code "length"})
     * @since 1.0.0
     */
    public SynapseResponse(String content, String model, int promptTokens, int completionTokens, String finishReason) {
        this.content = content;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.finishReason = finishReason;
    }

    /**
     * Returns the generated text content from the LLM.
     *
     * @return the response content, or {@code null} if not set
     * @since 1.0.0
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the generated text content from the LLM.
     *
     * @param content the response content
     * @since 1.0.0
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the identifier of the model that generated this response.
     *
     * @return the model identifier (e.g., {@code "gpt-4"}, {@code "claude-3-opus"})
     * @since 1.0.0
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the identifier of the model that generated this response.
     *
     * @param model the model identifier
     * @since 1.0.0
     */
    public void setModel(String model) {
        this.model = model;
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
     * Sets the number of tokens consumed by the input prompt.
     *
     * @param promptTokens the prompt token count
     * @since 1.0.0
     */
    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
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
     * Sets the number of tokens generated in the response.
     *
     * @param completionTokens the completion token count
     * @since 1.0.0
     */
    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    /**
     * Returns the reason the model stopped generating tokens.
     *
     * <p>Common finish reasons include:</p>
     * <ul>
     *   <li>{@code "stop"} - the model reached a natural stopping point</li>
     *   <li>{@code "length"} - the response hit the maximum token limit</li>
     *   <li>{@code "content_filter"} - the response was filtered by content moderation</li>
     * </ul>
     *
     * @return the finish reason string
     * @since 1.0.0
     */
    public String getFinishReason() {
        return finishReason;
    }

    /**
     * Sets the reason the model stopped generating tokens.
     *
     * @param finishReason the finish reason string
     * @since 1.0.0
     */
    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
