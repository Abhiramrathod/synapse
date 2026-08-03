package org.abhi.synapse.core;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * An {@link ISynapseHub} decorator that automatically routes around failed hubs.
 *
 * <p>Calls are attempted against the hubs in the order they were supplied. When
 * a hub fails with a {@link SynapseException} the next hub is tried, so a
 * single-provider application keeps working while that provider is down.
 * Typical use is primary + fallback providers:</p>
 *
 * <pre>{@code
 * ISynapseHub primary = new SynapseHub(primaryConfig);
 * ISynapseHub fallback = new SynapseHub(backupConfig);
 * ISynapseHub hub = new FallbackSynapseHub(primary, fallback);
 * }</pre>
 *
 * <p>Fallback applies to synchronous, typed, asynchronous, model-list, and
 * streaming submission. A stream that has already started delivering chunks on
 * a hub cannot be replayed on another, so mid-stream failures are propagated to
 * the caller rather than re-routed.</p>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see LoadBalancingSynapseHub
 */
public class FallbackSynapseHub extends AbstractDelegatingHub {

    public FallbackSynapseHub(ISynapseHub... hubs) {
        super(List.of(hubs));
    }

    public FallbackSynapseHub(List<ISynapseHub> hubs) {
        super(hubs);
    }

    @Override
    protected int nextHubIndex() {
        return 0;
    }

    @Override
    public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
        return withFallback(hub -> hub.sendPrompt(prompt, options));
    }

    @Override
    public <T> T sendPrompt(String prompt, Class<T> returnType, RequestOptions options) throws SynapseException {
        return withFallback(hub -> hub.sendPrompt(prompt, returnType, options));
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        return withFallback(hub -> hub.sendChat(messages, options));
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException {
        return withFallback(hub -> hub.chatCompletion(requestBody, options));
    }

    @Override
    public List<Model> getModelsList() throws SynapseException {
        return withFallback(hub -> hub.getModelsList());
    }

    @Override
    public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options)
            throws SynapseException {
        return asyncFallback(0, new AtomicReference<>(), hub -> hub.sendPromptAsync(prompt, options));
    }

    @Override
    public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options)
            throws SynapseException {
        return asyncFallback(0, new AtomicReference<>(), hub -> hub.sendChatAsync(messages, options));
    }

    @Override
    public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
        return streamWithFallback(hub -> hub.streamPrompt(prompt, listener));
    }

    @Override
    public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
        return streamWithFallback(hub -> hub.streamChat(messages, listener));
    }

    @Override
    public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
        return streamWithFallback(hub -> hub.streamCompletion(requestBody, listener));
    }

    @Override
    public Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException {
        return flowWithFallback(hub -> hub.streamChatAsFlow(messages));
    }

    @Override
    public Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
        return flowWithFallback(hub -> hub.streamPromptAsFlow(prompt));
    }

    private <R> R withFallback(SynapseCall<R> call) throws SynapseException {
        SynapseException lastError = null;
        for (ISynapseHub hub : hubs) {
            try {
                return call.call(hub);
            } catch (SynapseException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    private CompletableFuture<SynapseResponse> asyncFallback(int index, AtomicReference<Throwable> lastError,
                                                             Function<ISynapseHub, CompletableFuture<SynapseResponse>> call) {
        if (index >= hubs.size()) {
            Throwable last = lastError.get();
            return CompletableFuture.failedFuture(last instanceof SynapseException ? (SynapseException) last
                    : new SynapseException("All hubs failed", last, SynapseException.ExceptionType.SERVER_ERROR));
        }
        try {
            return call.apply(hubs.get(index)).handle((response, error) -> {
                if (error == null) {
                    return CompletableFuture.completedFuture(response);
                }
                lastError.set(unwrap(error));
                return asyncFallback(index + 1, lastError, call);
            }).thenCompose(Function.identity());
        } catch (SynapseException e) {
            lastError.set(e);
            return asyncFallback(index + 1, lastError, call);
        }
    }

    private StreamHandle streamWithFallback(SynapseCall<StreamHandle> call) throws SynapseException {
        SynapseException lastError = null;
        for (ISynapseHub hub : hubs) {
            try {
                return call.call(hub);
            } catch (SynapseException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    private Flow.Publisher<String> flowWithFallback(SynapseCall<Flow.Publisher<String>> call) throws SynapseException {
        SynapseException lastError = null;
        for (ISynapseHub hub : hubs) {
            try {
                return call.call(hub);
            } catch (SynapseException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    private static Throwable unwrap(Throwable error) {
        return error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
    }

    private interface SynapseCall<R> {
        R call(ISynapseHub hub) throws SynapseException;
    }
}
