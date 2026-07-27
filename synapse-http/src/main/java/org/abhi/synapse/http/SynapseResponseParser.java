package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses JSON response bodies from LLM API endpoints into {@link SynapseResponse} objects.
 *
 * <p>This class handles the extraction of response fields from the OpenAI-compatible
 * chat completion JSON response format, including the model name, content, finish reason,
 * and token usage statistics. Missing or malformed fields are handled gracefully with
 * sensible defaults.</p>
 *
 * <p>This is an internal class within the {@code synapse-http} module and is not
 * intended for direct use by library consumers.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseResponse
 * @see SynapseHub#sendChat(java.util.List)
 */
class SynapseResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code SynapseResponseParser} with the specified JSON mapper.
     *
     * @param objectMapper the {@link ObjectMapper} to use for JSON deserialization;
     *                     must not be {@code null}
     * @since 1.0.0
     */
    SynapseResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses a JSON response body string into a {@link SynapseResponse} object.
     *
     * <p>The parser extracts the following fields from the JSON response:</p>
     * <ul>
     *   <li>{@code model} - the model used for generation</li>
     *   <li>{@code choices[0].finish_reason} - the reason the model stopped generating</li>
     *   <li>{@code choices[0].message.content} - the generated content</li>
     *   <li>{@code usage.prompt_tokens} - number of tokens in the prompt</li>
     *   <li>{@code usage.completion_tokens} - number of tokens in the completion</li>
     * </ul>
     *
     * @param responseBody the JSON response body string; must not be {@code null}
     * @return the parsed {@link SynapseResponse}; never {@code null}
     * @throws SynapseException if the response body cannot be parsed as valid JSON
     *                          or if the expected structure is missing,
     *                          with exception type {@link SynapseException.ExceptionType#PARSE_ERROR}
     * @since 1.0.0
     */
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

    List<Model> parseModels(String responseBody) throws SynapseException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            List<Model> models = new ArrayList<>();
            for (JsonNode n : root.path("data")) {
                models.add(Model.builder()
                        .id(n.path("id").asText(null))
                        .object(n.path("object").asText(null))
                        .created(n.path("created").asLong(0))
                        .ownedBy(n.path("owned_by").asText(null))
                        .build());
            }
            return models;
        } catch (Exception e) {
            throw new SynapseException("Failed to parse models response", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }
}
