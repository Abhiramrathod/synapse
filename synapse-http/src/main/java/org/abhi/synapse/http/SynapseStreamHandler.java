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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SynapseStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(SynapseStreamHandler.class);

    private final SynapseHttpClient httpClient;
    private final ProviderAdapter adapter;

    SynapseStreamHandler(SynapseHttpClient httpClient, ProviderAdapter adapter) {
        this.httpClient = httpClient;
        this.adapter = adapter;
    }

    void handle(HttpRequest request, Consumer<String> onChunk, boolean enableLogging) throws SynapseException {
        handleWithStreamListener(request, StreamListener.of(onChunk), new CancellationToken(), enableLogging);
    }

    private void processLines(Stream<String> lines, CancellationToken token,
                               boolean enableLogging, Consumer<String> onContent, long[] usage) {
        lines.forEach(line -> {
            if (token != null && token.isCancelled()) return;
            String data = adapter.extractStreamData(line);
            if (data == null || adapter.isStreamDone(data)) return;
            if (adapter.isUsageChunk(data)) {
                long[] extracted = adapter.extractStreamUsage(data);
                if (extracted != null) { usage[0] = extracted[0]; usage[1] = extracted[1]; }
                return;
            }
            try {
                String content = adapter.extractContentFromStreamChunk(data);
                if (!content.isEmpty()) onContent.accept(content);
            } catch (Exception e) {
                if (enableLogging) log.warn("[Synapse] Failed to parse stream chunk: {}", data);
            }
        });
    }

    SynapseResponse handleWithStreamListener(HttpRequest request, StreamListener listener,
                                              CancellationToken token, boolean enableLogging)
            throws SynapseException {
        HttpResponse<Stream<String>> response = httpClient.sendStreaming(request);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new SynapseException(response.statusCode(), response.body().collect(Collectors.joining()));
        }

        StringBuilder accumulated = new StringBuilder();
        long[] usage = new long[2];
        try {
            processLines(response.body(), token, enableLogging, chunk -> {
                accumulated.append(chunk);
                listener.onChunk(chunk);
            }, usage);
        } catch (Exception e) {
            if (accumulated.length() > 0) {
                SynapseResponse partial = buildResponse(accumulated.toString(), usage);
                listener.onComplete(partial);
                return partial;
            }
            throw new SynapseException("Streaming request failed", e, SynapseException.ExceptionType.STREAMING_ERROR);
        }

        SynapseResponse full = buildResponse(accumulated.toString(), usage);
        listener.onComplete(full);
        return full;
    }

    void handleAsFlow(HttpRequest request, FlowPublisher<String> publisher,
                       CancellationToken token, boolean enableLogging) throws SynapseException {
        HttpResponse<Stream<String>> response = httpClient.sendStreaming(request);
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new SynapseException(response.statusCode(), response.body().collect(Collectors.joining()));
        }
        try {
            processLines(response.body(), token, enableLogging, publisher::submit, new long[2]);
        } finally {
            publisher.close();
        }
    }

    private SynapseResponse buildResponse(String content, long[] usage) {
        SynapseResponse r = new SynapseResponse();
        r.setContent(content);
        r.setPromptTokens((int) usage[0]);
        r.setCompletionTokens((int) usage[1]);
        r.setCorrelationId(java.util.UUID.randomUUID().toString());
        return r;
    }
}
