package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
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
        return buildChatBody(messages, temperature, maxTokens, modelName, streaming, null, null);
    }
    SynapseResponse parseResponse(String responseBody);
    List<Model> parseModels(String responseBody);
    String extractContentFromStreamChunk(String jsonData);
    boolean isStreamDone(String line);

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
