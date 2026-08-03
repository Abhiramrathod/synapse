package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.ResponseFormat;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.model.ToolDefinition;
import java.util.List;
import java.util.Map;

public interface ProviderAdapter {
    String providerName();
    String buildUrl(String baseUrl, String endpoint);
    Map<String, String> buildAuthHeaders(String apiKey);
    default Map<String, String> buildHeaders(String apiKey) {
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.putAll(buildAuthHeaders(apiKey));
        return headers;
    }

    /**
     * Builds the URL used to list available models.
     *
     * <p>The default assumes an OpenAI-compatible {@code /v1/models} layout.
     * Providers with a different models endpoint must override this method.</p>
     *
     * @param baseUrl the configured base URL; must not be {@code null}
     * @return the full models list URL
     */
    default String buildModelsUrl(String baseUrl) {
        String cleanUrl = baseUrl.replaceAll("/+$", "");
        return cleanUrl.endsWith("/v1") ? cleanUrl + "/models" : cleanUrl + "/v1/models";
    }
    Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
                                       int maxTokens, String modelName, boolean streaming,
                                       List<ToolDefinition> tools, String responseFormat);
    default Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
                                               int maxTokens, String modelName, boolean streaming) {
        return buildChatBody(messages, temperature, maxTokens, modelName, streaming,
                (List<ToolDefinition>) null, (String) null);
    }

    /**
     * Builds the chat request body with a structured {@link ResponseFormat}.
     *
     * <p>The default implementation forwards only the format type to
     * {@link #buildChatBody(List, double, int, String, boolean, List, String)}.
     * Providers that support native JSON Schema output (such as OpenAI) should
     * override this method to emit the full schema payload.</p>
     */
    default Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
                                               int maxTokens, String modelName, boolean streaming,
                                               List<ToolDefinition> tools, ResponseFormat responseFormat) {
        return buildChatBody(messages, temperature, maxTokens, modelName, streaming, tools,
                (String) (responseFormat != null ? responseFormat.getType() : null));
    }

    /**
     * Whether this provider supports native {@code json_schema} structured output.
     *
     * <p>When {@code false}, structured output falls back to injecting the schema
     * into the prompt text.</p>
     */
    default boolean supportsJsonSchemaStructuredOutput() {
        return true;
    }
    SynapseResponse parseResponse(String responseBody);
    List<Model> parseModels(String responseBody);
    String extractContentFromStreamChunk(String jsonData);
    boolean isStreamDone(String line);

    /**
     * Whether a single SSE payload is a stream usage chunk rather than content.
     *
     * <p>Providers that emit token usage as a dedicated SSE payload during
     * streaming (such as OpenAI, which sends an empty-{@code choices} chunk
     * carrying a {@code usage} object just before {@code [DONE]}) should
     * override this method. The default implementation reports {@code false}.</p>
     *
     * @param jsonData the raw JSON payload of the stream chunk; may be {@code null}
     * @return {@code true} if the chunk carries only usage statistics
     */
    default boolean isUsageChunk(String jsonData) {
        return false;
    }

    /**
     * Extracts token usage from a stream usage chunk.
     *
     * @param jsonData the raw JSON payload of the stream chunk; may be {@code null}
     * @return a two-element array {@code {promptTokens, completionTokens}}, or
     *         {@code null} if the chunk carries no usage statistics
     */
    default long[] extractStreamUsage(String jsonData) {
        return null;
    }

    /**
     * Extracts the raw JSON payload from a single SSE line.
     *
     * <p>The default implementation handles OpenAI-style lines prefixed with
     * {@code data: }. Providers with different framing must override this
     * method and return the JSON payload (or {@code null} for lines that
     * carry no payload, such as keep-alive comments or event metadata).</p>
     *
     * @param line the raw stream line; may be {@code null}
     * @return the JSON payload string, or {@code null} if the line carries none
     */
    default String extractStreamData(String line) {
        if (line == null) return null;
        String trimmed = line.trim();
        if (!trimmed.startsWith("data:")) return null;
        return trimmed.substring(5).trim();
    }
}
