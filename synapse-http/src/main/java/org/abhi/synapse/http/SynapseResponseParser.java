package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class SynapseResponseParser {

    private final ObjectMapper objectMapper;

    SynapseResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    SynapseResponse parse(String responseBody) throws SynapseException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            SynapseResponse response = new SynapseResponse();
            response.setModel(root.path("model").asText(null));
            response.setFinishReason(
                    root.path("choices").path(0).path("finish_reason").asText(null));
            response.setContent(
                    root.path("choices").path(0).path("message").path("content").asText(""));

            JsonNode usage = root.path("usage");
            response.setPromptTokens(usage.path("prompt_tokens").asInt(0));
            response.setCompletionTokens(usage.path("completion_tokens").asInt(0));

            return response;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse LLM response", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }
}
