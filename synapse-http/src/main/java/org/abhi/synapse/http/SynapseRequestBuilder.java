package org.abhi.synapse.http;

import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds HTTP requests for LLM API calls, including URL construction, request body
 * serialization, and {@link HttpRequest} assembly.
 *
 * <p>This class is responsible for constructing properly formatted requests that
 * conform to the OpenAI-compatible chat completion API schema. It handles URL
 * construction from the base URL and endpoint, message body assembly with model
 * parameters, and JSON serialization of the request body.</p>
 *
 * <p>This is an internal class within the {@code synapse-http} module and is not
 * intended for direct use by library consumers.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseConfig
 * @see SynapseHub
 */
class SynapseRequestBuilder {

    private final SynapseConfig config;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code SynapseRequestBuilder} with the specified configuration
     * and JSON mapper.
     *
     * @param config       the {@link SynapseConfig} containing endpoint and model settings;
     *                     must not be {@code null}
     * @param objectMapper the {@link ObjectMapper} to use for JSON serialization;
     *                     must not be {@code null}
     * @since 1.0.0
     */
    SynapseRequestBuilder(SynapseConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * Builds the full API URL by combining the base URL and endpoint from the
     * configuration.
     *
     * <p>Any trailing slashes in the base URL are removed before concatenation
     * to prevent double-slash issues.</p>
     *
     * @return the complete API URL as a {@link String}; never {@code null}
     * @since 1.0.0
     */
    String buildUrl() {
        return config.getBaseUrl().replaceAll("/+$", "") + config.getEndpoint();
    }

    /**
     * Builds a request body map from the provided chat messages, including
     * model parameters and optional streaming flag.
     *
     * <p>The returned map contains the following keys:</p>
     * <ul>
     *   <li>{@code model} - from {@link SynapseConfig#getModelName()}</li>
     *   <li>{@code messages} - the provided message list</li>
     *   <li>{@code temperature} - from {@link SynapseConfig#getTemperature()}</li>
     *   <li>{@code max_tokens} - from {@link SynapseConfig#getMaxTokens()}</li>
     *   <li>{@code stream} - (optional) {@code true} if streaming is enabled</li>
     * </ul>
     *
     * @param messages the list of {@link ChatMessage} objects to include;
     *                 must not be {@code null}
     * @param stream   {@code true} to include the {@code stream: true} parameter
     *                 in the body; {@code false} to omit it
     * @return a {@link Map} representing the request body; never {@code null}
     * @since 1.0.0
     */
    Map<String, Object> buildMessagesBody(List<ChatMessage> messages, boolean stream) {
        return buildMessagesBody(messages, stream, config.getModelName());
    }

    Map<String, Object> buildMessagesBody(List<ChatMessage> messages, boolean stream, String modelName) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("messages", messages);
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());
        if (stream) {
            body.put("stream", true);
        }
        return body;
    }

    String replaceModelInBody(String requestBody, String modelName) throws SynapseException {
        try {
            JsonNode root = objectMapper.readTree(requestBody);
            if (root instanceof ObjectNode) {
                ((ObjectNode) root).put("model", modelName);
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new SynapseException("Failed to replace model name in request body", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    /**
     * Serializes the given request body map into a JSON string using the configured
     * {@link ObjectMapper}.
     *
     * @param body the {@link Map} to serialize; must not be {@code null}
     * @return the JSON string representation of the body; never {@code null}
     * @throws SynapseException if serialization fails due to a JSON processing error,
     *                          with exception type {@link SynapseException.ExceptionType#PARSE_ERROR}
     * @since 1.0.0
     */
    String serializeBody(Map<String, Object> body) throws SynapseException {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new SynapseException("Failed to serialize request body", e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    /**
     * Builds a {@link HttpRequest} with the specified URL and JSON body, including
     * appropriate headers and timeout settings from the configuration.
     *
     * <p>The request is configured with:</p>
     * <ul>
     *   <li>{@code Content-Type: application/json}</li>
     *   <li>{@code Authorization: Bearer <api-key>}</li>
     *   <li>Timeout from {@link SynapseConfig#getRequestTimeout()}</li>
     *   <li>HTTP POST method with the provided body</li>
     * </ul>
     *
     * @param url  the full API URL to send the request to; must not be {@code null}
     * @param body the JSON string to send as the request body; must not be {@code null}
     * @return the constructed {@link HttpRequest}; never {@code null}
     * @since 1.0.0
     */
    HttpRequest buildPostRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .timeout(config.getRequestTimeout())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }


    HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .timeout(config.getRequestTimeout())
                .GET()
                .build();
    }
}
