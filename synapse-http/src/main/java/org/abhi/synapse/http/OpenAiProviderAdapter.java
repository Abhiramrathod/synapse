package org.abhi.synapse.http;

import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class OpenAiProviderAdapter implements ProviderAdapter {
    private final ObjectMapper objectMapper;
    public OpenAiProviderAdapter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    @Override public String providerName() { return "openai"; }
    @Override public String buildUrl(String baseUrl, String endpoint) { return baseUrl.replaceAll("/+$", "") + endpoint; }
    @Override public Map<String, String> buildAuthHeaders(String apiKey) {
        Map<String, String> h = new HashMap<>(); h.put("Authorization", "Bearer " + apiKey); return h;
    }
    @Override public Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
            int maxTokens, String modelName, boolean streaming, List<ToolDefinition> tools, String responseFormat) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName); body.put("messages", messages);
        body.put("temperature", temperature); body.put("max_tokens", maxTokens);
        if (streaming) body.put("stream", true);
        if (tools != null && !tools.isEmpty()) body.put("tools", tools);
        if (responseFormat != null) body.put("response_format", Map.of("type", responseFormat));
        return body;
    }
    @Override public SynapseResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            SynapseResponse response = new SynapseResponse();
            response.setModel(root.path("model").asText(null));
            response.setFinishReason(root.path("choices").path(0).path("finish_reason").asText(null));
            response.setContent(root.path("choices").path(0).path("message").path("content").asText(""));
            JsonNode usage = root.path("usage");
            response.setPromptTokens(usage.path("prompt_tokens").asInt(0));
            response.setCompletionTokens(usage.path("completion_tokens").asInt(0));
            JsonNode tc = root.path("choices").path(0).path("message").path("tool_calls");
            if (tc.isArray() && !tc.isEmpty()) {
                List<ToolCall> calls = new ArrayList<>();
                for (JsonNode t : tc) calls.add(new ToolCall(t.path("id").asText(null),
                        t.path("type").asText("function"), t.path("function").path("name").asText(null),
                        t.path("function").path("arguments").asText(null)));
                response.setToolCalls(calls);
            }
            return response;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse OpenAI response", e, SynapseException.ExceptionType.PARSE_ERROR);
        }
    }
    @Override public List<Model> parseModels(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            List<Model> models = new ArrayList<>();
            for (JsonNode n : root.path("data")) models.add(Model.builder()
                    .id(n.path("id").asText(null)).object(n.path("object").asText(null))
                    .created(n.path("created").asLong(0)).ownedBy(n.path("owned_by").asText(null)).build());
            return models;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse models response", e, SynapseException.ExceptionType.PARSE_ERROR);
        }
    }
    @Override public String extractContentFromStreamChunk(String jsonData) {
        try { return objectMapper.readTree(jsonData).path("choices").path(0).path("delta").path("content").asText(""); }
        catch (Exception e) { return ""; }
    }
    @Override public boolean isStreamDone(String line) {
        if (line == null) return true;
        String t = line.trim();
        return t.equals("[DONE]") || t.equals("\"message_stop\"") || t.contains("\"finish_reason\"");
    }
}
