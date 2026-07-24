package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SynapseRequestBuilder {

    private final SynapseConfig config;
    private final ObjectMapper objectMapper;

    SynapseRequestBuilder(SynapseConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    String buildUrl() {
        return config.getBaseUrl().replaceAll("/+$", "") + config.getEndpoint();
    }

    Map<String, Object> buildMessagesBody(List<ChatMessage> messages, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModelName());
        body.put("messages", messages);
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());
        if (stream) {
            body.put("stream", true);
        }
        return body;
    }

    String serializeBody(Map<String, Object> body) throws SynapseException {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new SynapseException("Failed to serialize request body", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    HttpRequest buildRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .timeout(config.getRequestTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
