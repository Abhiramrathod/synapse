package org.abhi.synapse.core;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamFlowTest {

    @Test
    void joinAggregatesAllChunks() throws Exception {
        StreamFlow<String> flow = StreamFlow.of(publisherOf("Hel", "lo ", "world"));

        String joined = flow.join().get();

        assertThat(joined).isEqualTo("Hello world");
    }

    @Test
    void joinWithDelimiter() throws Exception {
        StreamFlow<String> flow = StreamFlow.of(publisherOf("a", "b", "c"));

        String joined = flow.join("|").get();

        assertThat(joined).isEqualTo("a|b|c");
    }

    @Test
    void toListCollectsEveryElement() throws Exception {
        StreamFlow<String> flow = StreamFlow.of(publisherOf("x", "y", "z"));

        List<String> items = flow.toList().get();

        assertThat(items).containsExactly("x", "y", "z");
    }

    @Test
    void mapTransformsElements() throws Exception {
        StreamFlow<Integer> flow = StreamFlow.of(publisherOf("1", "2", "3")).map(Integer::parseInt);

        assertThat(flow.toList().get()).containsExactly(1, 2, 3);
    }

    @Test
    void filterDropsRejectedElements() throws Exception {
        StreamFlow<String> flow = StreamFlow.of(publisherOf("one", "", "two", " ", "three"))
                .filter(chunk -> !chunk.isBlank());

        assertThat(flow.toList().get()).containsExactly("one", "two", "three");
    }

    @Test
    void forEachConsumesEveryChunk() throws Exception {
        AtomicInteger counter = new AtomicInteger();

        StreamFlow.of(publisherOf("a", "b", "c")).forEach(chunk -> counter.incrementAndGet()).get();

        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void countCountsElements() throws Exception {
        Long count = StreamFlow.of(publisherOf("a", "b", "c", "d")).count().get();

        assertThat(count).isEqualTo(4);
    }

    @Test
    void onErrorReturnEmitsFallbackAndCompletes() throws Exception {
        StreamFlow<String> flow = StreamFlow.<String>of(failingPublisher(new SynapseException("down",
                SynapseException.ExceptionType.SERVER_ERROR))).onErrorReturn("fallback");

        List<String> items = flow.toList().get();

        assertThat(items).containsExactly("fallback");
    }

    @Test
    void blockLastReturnsLastElement() throws SynapseException {
        assertThat(StreamFlow.of(publisherOf("a", "b", "c")).blockLast()).isEqualTo("c");
    }

    @Test
    void blockFirstReturnsFirstElement() throws SynapseException {
        assertThat(StreamFlow.of(publisherOf("a", "b", "c")).blockFirst()).isEqualTo("a");
    }

    @Test
    void blockLastOnEmptyStreamReturnsNull() throws SynapseException {
        assertThat(StreamFlow.<String>of(publisherOf(List.<String>of())).blockLast()).isNull();
    }

    @Test
    void blockLastUnwrapsSynapseException() {
        assertThatThrownBy(() -> StreamFlow.of(failingPublisher(new SynapseException("boom",
                SynapseException.ExceptionType.STREAMING_ERROR))).blockLast())
                .isInstanceOf(SynapseException.class)
                .hasMessage("boom");
    }

    @Test
    void ofPromptStreamsFromHub() throws Exception {
        HubStub hub = new HubStub(publisherOf("Ping", " ", "Pong"));

        String joined = StreamFlow.ofPrompt(hub, "Hi").join().get();

        assertThat(joined).isEqualTo("Ping Pong");
    }

    @Test
    void ofChatStreamsFromHub() throws Exception {
        HubStub hub = new HubStub(publisherOf("chunk"));

        String joined = StreamFlow.ofChat(hub, List.of(ChatMessage.user("Hi"))).join().get();

        assertThat(joined).isEqualTo("chunk");
    }

    @Test
    void subscribeWrapsRawPublisher() throws Exception {
        CompletableFuture<List<String>> received = new CompletableFuture<>();
        StreamFlow<String> flow = StreamFlow.of(publisherOf("a", "b"));

        flow.subscribe(new Flow.Subscriber<>() {
            private final List<String> buffer = new java.util.ArrayList<>();

            @Override public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }
            @Override public void onNext(String item) { buffer.add(item); }
            @Override public void onError(Throwable throwable) { received.completeExceptionally(throwable); }
            @Override public void onComplete() { received.complete(List.copyOf(buffer)); }
        });

        assertThat(received.get()).containsExactly("a", "b");
    }

    static <T> Flow.Publisher<T> publisherOf(T... items) {
        return publisherOf(List.of(items));
    }

    static <T> Flow.Publisher<T> publisherOf(List<T> items) {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override public void request(long n) { }
                @Override public void cancel() { }
            });
            for (T item : items) {
                subscriber.onNext(item);
            }
            subscriber.onComplete();
        };
    }

    static <T> Flow.Publisher<T> failingPublisher(Throwable error) {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override public void request(long n) { }
                @Override public void cancel() { }
            });
            subscriber.onError(error);
        };
    }

    private static final class HubStub implements ISynapseHub {
        private final Flow.Publisher<String> flow;

        HubStub(Flow.Publisher<String> flow) { this.flow = flow; }

        @Override public SynapseResponse sendPrompt(String prompt, RequestOptions options) {
            throw new UnsupportedOperationException();
        }
        @Override public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) {
            throw new UnsupportedOperationException();
        }
        @Override public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) {
            throw new UnsupportedOperationException();
        }
        @Override public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) {
            throw new UnsupportedOperationException();
        }
        @Override public SynapseResponse chatCompletion(String requestBody, RequestOptions options) {
            throw new UnsupportedOperationException();
        }
        @Override public StreamHandle streamPrompt(String prompt, StreamListener listener) {
            throw new UnsupportedOperationException();
        }
        @Override public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) {
            throw new UnsupportedOperationException();
        }
        @Override public StreamHandle streamCompletion(String requestBody, StreamListener listener) {
            throw new UnsupportedOperationException();
        }
        @Override public Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) {
            return flow;
        }
        @Override public Flow.Publisher<String> streamPromptAsFlow(String prompt) {
            return flow;
        }
        @Override public List<Model> getModelsList() {
            throw new UnsupportedOperationException();
        }
        @Override public void close() { }
    }
}
