package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Handles Server-Sent Events (SSE) streaming responses from LLM API endpoints.
 *
 * <p>This class processes the streaming response body line by line, parsing SSE
 * {@code data:} events and extracting content deltas from the JSON payload.
 * Each non-empty content delta is delivered to the provided {@link Consumer}
 * callback. The stream terminates when a {@code [DONE]} sentinel is received.</p>
 *
 * <p>This is an internal class within the {@code synapse-http} module and is not
 * intended for direct use by library consumers.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseHttpClient
 * @see SynapseHub#streamCompletion(String, Consumer)
 */
class SynapseStreamHandler {

    private static final Logger LOGGER = Logger.getLogger(SynapseStreamHandler.class.getName());

    private final SynapseHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code SynapseStreamHandler} with the specified HTTP client
     * and JSON mapper.
     *
     * @param httpClient   the {@link SynapseHttpClient} to use for sending streaming requests;
     *                     must not be {@code null}
     * @param objectMapper the {@link ObjectMapper} to use for parsing JSON chunks;
     *                     must not be {@code null}
     * @since 1.0.0
     */
    SynapseStreamHandler(SynapseHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Handles the streaming response for the given HTTP request, parsing each SSE event
     * and delivering content deltas to the provided callback.
     *
     * <p>The method sends the request as a streaming request via {@link SynapseHttpClient},
     * then processes the response body line by line. Lines starting with {@code data: }
     * are parsed as JSON to extract the content delta from
     * {@code choices[0].delta.content}. The {@code [DONE]} sentinel signals the end
     * of the stream.</p>
     *
     * <p>If the HTTP response status is not in the 2xx range, the entire response body
     * is collected and a {@link SynapseException} is thrown with the status code and body.</p>
     *
     * @param request      the {@link HttpRequest} to send; must not be {@code null}
     * @param onChunk      the {@link Consumer} callback that receives each text content delta;
     *                     must not be {@code null}
     * @param enableLogging if {@code true}, failed chunk parsing attempts are logged at
     *                      WARNING level
     * @throws SynapseException if the HTTP response status is not in the 2xx range,
     *                          or if the streaming request fails
     * @since 1.0.0
     */
    void handle(HttpRequest request, Consumer<String> onChunk, boolean enableLogging)
            throws SynapseException {
        HttpResponse<java.util.stream.Stream<String>> response = httpClient.sendStreaming(request);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body().collect(Collectors.joining());
            throw new SynapseException(response.statusCode(), body);
        }

        response.body().forEach(line -> {
            if (line.startsWith("data: ")) {
                String data = line.substring(6).trim();
                if (data.equals("[DONE]")) {
                    return;
                }
                try {
                    JsonNode node = objectMapper.readTree(data);
                    String content = node.path("choices").path(0)
                            .path("delta").path("content").asText("");
                    if (!content.isEmpty()) {
                        onChunk.accept(content);
                    }
                } catch (Exception e) {
                    if (enableLogging) {
                        LOGGER.log(Level.WARNING, "[Synapse] Failed to parse stream chunk: " + data);
                    }
                }
            }
        });
    }
}
