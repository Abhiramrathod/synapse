package org.abhi.synapse.http;

import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class OpenAiProviderAdapter implements ProviderAdapter {
    private final ObjectMapper objectMapper;
    public OpenAiProviderAdapter() { this(new ObjectMapper()); }
    public OpenAiProviderAdapter(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }
    @Override public String providerName() { return "openai"; }
    @Override public String buildUrl(String baseUrl, String endpoint) { return baseUrl.replaceAll("/+$", "") + endpoint; }
    @Override public String buildModelsUrl(String baseUrl) {
        String cleanUrl = baseUrl.replaceAll("/+$", "");
        return cleanUrl.endsWith("/v1") ? cleanUrl + "/models" : cleanUrl + "/v1/models";
    }
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

    @Override public Map<String, Object> buildChatBody(List<ChatMessage> messages, double temperature,
            int maxTokens, String modelName, boolean streaming, List<ToolDefinition> tools,
            ResponseFormat responseFormat) {
        Map<String, Object> body = buildChatBody(messages, temperature, maxTokens, modelName,
                streaming, tools, responseFormat != null ? responseFormat.getType() : null);
        if (responseFormat != null && "json_schema".equals(responseFormat.getType())) {
            try {
                JsonNode schema = objectMapper.readTree(responseFormat.getSchemaJson());
                Map<String, Object> schemaConfig = new HashMap<>();
                schemaConfig.put("name", responseFormat.getName());
                schemaConfig.put("schema", schema);
                body.put("response_format", Map.of("type", "json_schema", "json_schema", schemaConfig));
            } catch (Exception e) {
                throw new SynapseException("Invalid JSON Schema for structured output", e,
                        SynapseException.ExceptionType.PARSE_ERROR);
            }
        }
        if (Boolean.TRUE.equals(streaming) && body.containsKey("stream")) {
            body.put("stream_options", Map.of("include_usage", true));
        }
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
    @Override public boolean isUsageChunk(String jsonData) {
        if (jsonData == null) return false;
        try { return objectMapper.readTree(jsonData).path("usage").isObject(); }
        catch (Exception e) { return false; }
    }
    @Override public long[] extractStreamUsage(String jsonData) {
        if (jsonData == null) return null;
        try {
            JsonNode usage = objectMapper.readTree(jsonData).path("usage");
            if (!usage.isObject()) return null;
            return new long[]{usage.path("prompt_tokens").asLong(0), usage.path("completion_tokens").asLong(0)};
        } catch (Exception e) { return null; }
    }
    @Override public boolean isStreamDone(String line) {
        if (line == null) return true;
        String t = line.trim();
        return t.equals("[DONE]") || t.equals("\"message_stop\"") || t.contains("\"finish_reason\"");
    }
}
