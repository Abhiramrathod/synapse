package org.abhi.synapse.http;

import org.abhi.synapse.core.ProviderAdapter;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.CancellationToken;
import org.abhi.synapse.core.model.SynapseResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

class SynapseStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(SynapseStreamHandler.class);

    private final SynapseHttpClient httpClient;
    private final ProviderAdapter adapter;

    SynapseStreamHandler(SynapseHttpClient httpClient, ProviderAdapter adapter) {
        this.httpClient = httpClient;
        this.adapter = adapter;
    }

    void handle(HttpRequest request, java.util.function.Consumer<String> onChunk, boolean enableLogging)
            throws SynapseException {
        handleWithStreamListener(request, StreamListener.of(onChunk), new CancellationToken(), enableLogging);
    }

    SynapseResponse handleWithStreamListener(HttpRequest request, StreamListener listener,
                                              CancellationToken token, boolean enableLogging)
            throws SynapseException {
        HttpResponse<java.util.stream.Stream<String>> response = httpClient.sendStreaming(request);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body().collect(Collectors.joining());
            throw new SynapseException(response.statusCode(), body);
        }

        StringBuilder accumulatedContent = new StringBuilder();
        try {
            response.body().forEach(line -> {
                if (token != null && token.isCancelled()) {
                    return;
                }
                String data = adapter.extractStreamData(line);
                if (data == null || adapter.isStreamDone(data)) {
                    return;
                }
                try {
                    String content = adapter.extractContentFromStreamChunk(data);
                    if (!content.isEmpty()) {
                        accumulatedContent.append(content);
                        listener.onChunk(content);
                    }
                } catch (Exception e) {
                    if (enableLogging) {
                        log.warn("[Synapse] Failed to parse stream chunk: {}", data);
                    }
                }
            });
        } catch (Exception e) {
            if (accumulatedContent.length() > 0) {
                SynapseResponse partial = new SynapseResponse();
                partial.setContent(accumulatedContent.toString());
                partial.setCorrelationId(java.util.UUID.randomUUID().toString());
                listener.onComplete(partial);
                return partial;
            }
            throw new SynapseException("Streaming request failed", e,
                    SynapseException.ExceptionType.STREAMING_ERROR);
        }

        SynapseResponse fullResponse = new SynapseResponse();
        fullResponse.setContent(accumulatedContent.toString());
        fullResponse.setCorrelationId(java.util.UUID.randomUUID().toString());
        listener.onComplete(fullResponse);
        return fullResponse;
    }

    void handleAsFlow(HttpRequest request, FlowPublisher<String> publisher,
                       CancellationToken token, boolean enableLogging) throws SynapseException {
        HttpResponse<java.util.stream.Stream<String>> response = httpClient.sendStreaming(request);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = response.body().collect(Collectors.joining());
            throw new SynapseException(response.statusCode(), body);
        }

        try {
            response.body().forEach(line -> {
                if (token != null && token.isCancelled()) {
                    return;
                }
                String data = adapter.extractStreamData(line);
                if (data == null || adapter.isStreamDone(data)) {
                    return;
                }
                try {
                    String content = adapter.extractContentFromStreamChunk(data);
                    if (!content.isEmpty()) {
                        publisher.submit(content);
                    }
                } catch (Exception e) {
                    if (enableLogging) {
                        log.warn("[Synapse] Failed to parse stream chunk: {}", data);
                    }
                }
            });
        } finally {
            publisher.close();
        }
    }
}
