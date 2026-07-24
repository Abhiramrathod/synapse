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

class SynapseStreamHandler {

    private static final Logger LOGGER = Logger.getLogger(SynapseStreamHandler.class.getName());

    private final SynapseHttpClient httpClient;
    private final ObjectMapper objectMapper;

    SynapseStreamHandler(SynapseHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

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
