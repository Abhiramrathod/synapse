package org.abhi.synapse.core;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Base class for {@link ISynapseHub} decorators that route calls across a set
 * of underlying hubs.
 *
 * <p>Concrete subclasses control routing by overriding {@link #nextHubIndex()}
 * (round-robin) or by reimplementing individual methods entirely (fallback).
 * All multi-call routing is thread-safe.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see FallbackSynapseHub
 * @see LoadBalancingSynapseHub
 */
public abstract class AbstractDelegatingHub implements ISynapseHub {

    protected final List<ISynapseHub> hubs;

    protected AbstractDelegatingHub(List<ISynapseHub> hubs) {
        if (hubs == null || hubs.isEmpty()) {
            throw new IllegalArgumentException("At least one hub is required");
        }
        this.hubs = List.copyOf(hubs);
    }

    /**
     * Returns the index of the hub that should serve the next call.
     *
     * @return an index into {@link #hubs}
     */
    protected abstract int nextHubIndex();

    @Override
    public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
        return hubs.get(nextHubIndex()).sendPrompt(prompt, options);
    }

    @Override
    public <T> T sendPrompt(String prompt, Class<T> returnType, RequestOptions options) throws SynapseException {
        return hubs.get(nextHubIndex()).sendPrompt(prompt, returnType, options);
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        return hubs.get(nextHubIndex()).sendChat(messages, options);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options)
            throws SynapseException {
        return hubs.get(nextHubIndex()).sendPromptAsync(prompt, options);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options)
            throws SynapseException {
        return hubs.get(nextHubIndex()).sendChatAsync(messages, options);
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException {
        return hubs.get(nextHubIndex()).chatCompletion(requestBody, options);
    }

    @Override
    public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
        return hubs.get(nextHubIndex()).streamPrompt(prompt, listener);
    }

    @Override
    public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
        return hubs.get(nextHubIndex()).streamChat(messages, listener);
    }

    @Override
    public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
        return hubs.get(nextHubIndex()).streamCompletion(requestBody, listener);
    }

    @Override
    public Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException {
        return hubs.get(nextHubIndex()).streamChatAsFlow(messages);
    }

    @Override
    public Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
        return hubs.get(nextHubIndex()).streamPromptAsFlow(prompt);
    }

    @Override
    public List<Model> getModelsList() throws SynapseException {
        return hubs.get(nextHubIndex()).getModelsList();
    }

    @Override
    public void close() {
        for (ISynapseHub hub : hubs) {
            try {
                hub.close();
            } catch (Exception ignored) {
                // never let one failing hub block the others from closing
            }
        }
    }
}
