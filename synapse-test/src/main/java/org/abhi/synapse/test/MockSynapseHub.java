package org.abhi.synapse.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.abhi.synapse.core.CancellationToken;
import org.abhi.synapse.core.ISynapseHub;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.StreamHandle;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory {@link ISynapseHub} for unit testing client code without any network I/O.
 *
 * <p>Stub the behavior you expect and assert on what was recorded:</p>
 * <pre>{@code
 * MockSynapseHub hub = MockSynapseHub.withResponse("Hello!");
 *
 * SynapseResponse response = hub.sendPrompt("Hi", null);
 * assertThat(response.getContent()).isEqualTo("Hello!");
 *
 * hub.verifyCalled(1);
 * assertThat(hub.recordedPrompts()).containsExactly("Hi");
 * }</pre>
 *
 * <p>Streaming stubs emit configured chunks and then complete. All recording is
 * thread-safe so the mock is safe to use from async test code.</p>
 */
public class MockSynapseHub implements ISynapseHub {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Stub> stubs = new ArrayList<>();
    private final List<String> recordedPrompts = new CopyOnWriteArrayList<>();
    private final List<List<ChatMessage>> recordedChats = new CopyOnWriteArrayList<>();
    private final List<String> recordedBodies = new CopyOnWriteArrayList<>();
    private final List<Model> models = new CopyOnWriteArrayList<>();
    private final AtomicInteger callCount = new AtomicInteger();
    private int stubIndex;
    private List<String> streamChunks = new ArrayList<>();

    private static final class Stub {
        final SynapseResponse response;
        final SynapseException error;

        Stub(SynapseResponse response) {
            this.response = response;
            this.error = null;
        }

        Stub(SynapseException error) {
            this.response = null;
            this.error = error;
        }
    }

    /**
     * Creates a hub whose next calls return a response with the given content.
     *
     * @param content the stubbed response content
     * @return a new mock hub
     */
    public static MockSynapseHub withResponse(String content) {
        return new MockSynapseHub().stubResponse(content);
    }

    /**
     * Creates a hub whose next calls throw the given error.
     *
     * @param error the stubbed exception
     * @return a new mock hub
     */
    public static MockSynapseHub withError(SynapseException error) {
        return new MockSynapseHub().stubError(error);
    }

    /** Enqueues a stub response with the given content. */
    public synchronized MockSynapseHub stubResponse(String content) {
        SynapseResponse response = new SynapseResponse();
        response.setContent(content);
        return stubResponse(response);
    }

    /** Enqueues a stub response. */
    public synchronized MockSynapseHub stubResponse(SynapseResponse response) {
        stubs.add(new Stub(response));
        return this;
    }

    /** Enqueues a stub error that subsequent calls will throw. */
    public synchronized MockSynapseHub stubError(SynapseException error) {
        stubs.add(new Stub(error));
        return this;
    }

    /** Sets the chunks emitted by streaming methods. */
    public synchronized MockSynapseHub stubStreaming(List<String> chunks) {
        this.streamChunks = new ArrayList<>(chunks);
        return this;
    }

    /** Registers models returned by {@link #getModelsList()}. */
    public synchronized MockSynapseHub stubModels(List<Model> models) {
        this.models.addAll(models);
        return this;
    }

    /** Returns the prompts recorded by {@code sendPrompt}, {@code sendPromptAsync} and {@code streamPrompt}. */
    public List<String> recordedPrompts() {
        return List.copyOf(recordedPrompts);
    }

    /** Returns the message lists recorded by chat and streaming methods. */
    public List<List<ChatMessage>> recordedChats() {
        return List.copyOf(recordedChats);
    }

    /** Returns the raw bodies recorded by {@code chatCompletion} and {@code streamCompletion}. */
    public List<String> recordedBodies() {
        return List.copyOf(recordedBodies);
    }

    /** Returns the total number of calls made to this mock. */
    public int callCount() {
        return callCount.get();
    }

    /**
     * Asserts that the mock has been called exactly the given number of times.
     *
     * @param expected the expected number of calls
     * @return this mock, for chaining
     * @throws AssertionError if the actual call count differs
     */
    public MockSynapseHub verifyCalled(int expected) {
        int actual = callCount.get();
        if (actual != expected) {
            throw new AssertionError("Expected " + expected + " calls but got " + actual);
        }
        return this;
    }

    /** Clears all stubs and recorded state. */
    public synchronized MockSynapseHub reset() {
        stubs.clear();
        stubIndex = 0;
        streamChunks.clear();
        recordedPrompts.clear();
        recordedChats.clear();
        recordedBodies.clear();
        models.clear();
        callCount.set(0);
        return this;
    }

    private synchronized Stub nextStub() {
        if (stubIndex < stubs.size()) {
            return stubs.get(stubIndex++);
        }
        if (!stubs.isEmpty()) {
            return stubs.get(stubs.size() - 1);
        }
        return new Stub(new SynapseResponse());
    }

    private synchronized List<String> streamChunksCopy() {
        return List.copyOf(streamChunks);
    }

    private static SynapseResponse deliver(Stub stub) {
        return stub.response;
    }

    @Override
    public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
        recordedPrompts.add(prompt);
        recordedChats.add(List.of(ChatMessage.user(prompt)));
        Stub stub = nextStub();
        callCount.incrementAndGet();
        if (stub.error != null) {
            throw stub.error;
        }
        return deliver(stub);
    }

    @Override
    public <T> T sendPrompt(String prompt, Class<T> returnType, RequestOptions options) throws SynapseException {
        SynapseResponse response = sendPrompt(prompt, options);
        try {
            return objectMapper.readValue(response.getContent(), returnType);
        } catch (Exception e) {
            throw new SynapseException("Failed to parse stubbed response as " + returnType.getSimpleName(), e,
                    SynapseException.ExceptionType.PARSE_ERROR);
        }
    }

    @Override
    public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        recordedChats.add(List.copyOf(messages));
        Stub stub = nextStub();
        callCount.incrementAndGet();
        if (stub.error != null) {
            throw stub.error;
        }
        return deliver(stub);
    }

    @Override
    public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException {
        try {
            return CompletableFuture.completedFuture(sendPrompt(prompt, options));
        } catch (SynapseException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
        try {
            return CompletableFuture.completedFuture(sendChat(messages, options));
        } catch (SynapseException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException {
        recordedBodies.add(requestBody);
        Stub stub = nextStub();
        callCount.incrementAndGet();
        if (stub.error != null) {
            throw stub.error;
        }
        return deliver(stub);
    }

    @Override
    public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
        recordedPrompts.add(prompt);
        return streamToListener(listener);
    }

    @Override
    public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
        recordedChats.add(List.copyOf(messages));
        return streamToListener(listener);
    }

    @Override
    public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
        recordedBodies.add(requestBody);
        return streamToListener(listener);
    }

    private StreamHandle streamToListener(StreamListener listener) {
        callCount.incrementAndGet();
        CancellationToken token = new CancellationToken();
        CompletableFuture<SynapseResponse> future = new CompletableFuture<>();
        Stub stub = nextStub();

        if (stub.error != null) {
            listener.onError(stub.error);
            future.completeExceptionally(stub.error);
            return new StreamHandle(token, future);
        }

        StringBuilder aggregated = new StringBuilder();
        for (String chunk : streamChunksCopy()) {
            if (token.isCancelled()) {
                SynapseException ex = new SynapseException("Stream cancelled by caller",
                        SynapseException.ExceptionType.STREAMING_ERROR);
                listener.onError(ex);
                future.completeExceptionally(ex);
                return new StreamHandle(token, future);
            }
            listener.onChunk(chunk);
            aggregated.append(chunk);
        }

        SynapseResponse full = stub.response != null ? stub.response : new SynapseResponse();
        full.setContent(aggregated.toString());
        listener.onComplete(full);
        future.complete(full);
        return new StreamHandle(token, future);
    }

    @Override
    public Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException {
        recordedChats.add(List.copyOf(messages));
        return flowPublisher();
    }

    @Override
    public Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
        recordedPrompts.add(prompt);
        return flowPublisher();
    }

    private Flow.Publisher<String> flowPublisher() {
        List<String> chunks = streamChunksCopy();
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override public void request(long n) {
                    try {
                        long remaining = n;
                        for (String chunk : chunks) {
                            if (remaining <= 0) {
                                break;
                            }
                            subscriber.onNext(chunk);
                            remaining--;
                        }
                        subscriber.onComplete();
                    } catch (Throwable t) {
                        subscriber.onError(t);
                    }
                }

                @Override public void cancel() {
                }
            });
        };
    }

    @Override
    public List<Model> getModelsList() throws SynapseException {
        return List.copyOf(models);
    }

    @Override
    public void close() {
        // no-op; the mock holds no external resources
    }
}
