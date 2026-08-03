package org.abhi.synapse.test;

import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.StreamHandle;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

/** Test double wrapping {@link MockSynapseHub} with call/close counters. */
final class RecordingHub implements ISynapseHub {
    final AtomicInteger closeCount = new AtomicInteger();
    final AtomicInteger streamCount = new AtomicInteger();
    final MockSynapseHub delegate;

    RecordingHub(String content) {
        this.delegate = new MockSynapseHub().stubResponse(content);
    }

    RecordingHub(SynapseException error) {
        this.delegate = new MockSynapseHub().stubError(error);
    }

    @Override public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
        return delegate.sendPrompt(prompt, options);
    }
    @Override public <T> T sendPrompt(String prompt, Class<T> returnType, RequestOptions options) throws SynapseException {
        return delegate.sendPrompt(prompt, returnType, options);
    }
    @Override public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        return delegate.sendChat(messages, options);
    }
    @Override public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException {
        return delegate.sendPromptAsync(prompt, options);
    }
    @Override public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        return delegate.sendChatAsync(messages, options);
    }
    @Override public SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException {
        return delegate.chatCompletion(requestBody, options);
    }
    @Override public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
        streamCount.incrementAndGet();
        return delegate.streamPrompt(prompt, listener);
    }
    @Override public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
        streamCount.incrementAndGet();
        return delegate.streamChat(messages, listener);
    }
    @Override public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
        streamCount.incrementAndGet();
        return delegate.streamCompletion(requestBody, listener);
    }
    @Override public Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException {
        return delegate.streamChatAsFlow(messages);
    }
    @Override public Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
        return delegate.streamPromptAsFlow(prompt);
    }
    @Override public List<Model> getModelsList() throws SynapseException {
        return delegate.getModelsList();
    }
    @Override public void close() {
        closeCount.incrementAndGet();
    }
}
