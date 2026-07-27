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
}
