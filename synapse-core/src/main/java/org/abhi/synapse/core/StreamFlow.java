package org.abhi.synapse.core;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Fluent utilities for consuming streaming LLM tokens without writing
 * {@link Flow.Subscriber} boilerplate.
 *
 * <p>Wraps a {@link Flow.Publisher} and provides a small set of reactive
 * operators plus blocking/async terminal operations. Streams are lazy: nothing
 * is consumed until a terminal operation ({@link #toList()}, {@link #join()},
 * {@link #forEach(Consumer)}, {@link #blockLast()} ...) subscribes.</p>
 *
 * <pre>{@code
 * // Consume chunks without any Subscriber boilerplate
 * hub.streamPromptAsFlow("Tell me a story")
 *     .filter(chunk -> !chunk.isBlank())
 *     .map(String::trim)
 *     .forEach(chunk -> System.out.print(chunk));
 *
 * // Or collect the whole stream into one string
 * String fullText = StreamFlow.of(hub.streamPromptAsFlow("Hello"))
 *         .join()
 *         .join();
 * }</pre>
 *
 * @param <T> the element type flowing through the stream
 * @author Abhiram Rathod
 * @since 1.0.0
 */
public final class StreamFlow<T> {

    private final Flow.Publisher<T> publisher;

    private StreamFlow(Flow.Publisher<T> publisher) {
        this.publisher = publisher;
    }

    /**
     * Wraps a raw {@link Flow.Publisher} in a fluent {@link StreamFlow}.
     *
     * @param publisher the source publisher; must not be {@code null}
     * @param <T>       the element type
     * @return a fluent stream over the publisher
     */
    public static <T> StreamFlow<T> of(Flow.Publisher<T> publisher) {
        if (publisher == null) {
            throw new IllegalArgumentException("publisher must not be null");
        }
        return new StreamFlow<>(publisher);
    }

    /**
     * Streams a single prompt and wraps it in a fluent {@link StreamFlow}.
     *
     * @param hub    the hub to stream from
     * @param prompt the user's prompt text
     * @return a fluent stream of response text chunks
     * @throws SynapseException if the request cannot be submitted
     */
    public static StreamFlow<String> ofPrompt(ISynapseHub hub, String prompt) throws SynapseException {
        return of(hub.streamPromptAsFlow(prompt));
    }

    /**
     * Streams a multi-turn chat and wraps it in a fluent {@link StreamFlow}.
     *
     * @param hub      the hub to stream from
     * @param messages the conversation history
     * @return a fluent stream of response text chunks
     * @throws SynapseException if the request cannot be submitted
     */
    public static StreamFlow<String> ofChat(ISynapseHub hub, List<ChatMessage> messages) throws SynapseException {
        return of(hub.streamChatAsFlow(messages));
    }

    /**
     * Returns a stream that only passes through elements matching the predicate.
     *
     * @param predicate the test applied to each element
     * @return a filtered stream
     */
    public StreamFlow<T> filter(Predicate<? super T> predicate) {
        if (predicate == null) {
            throw new IllegalArgumentException("predicate must not be null");
        }
        return of(new MapOrFilterPublisher<>(publisher, predicate, null));
    }

    /**
     * Returns a stream that transforms each element with the given mapper.
     *
     * @param mapper the transformation to apply
     * @param <R>    the mapped element type
     * @return a mapped stream
     */
    public <R> StreamFlow<R> map(Function<? super T, ? extends R> mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        return of(new MapOrFilterPublisher<>(publisher, null, mapper));
    }

    /**
     * Returns a stream that suppresses errors and emits {@code fallback} instead,
     * then completes normally.
     *
     * @param fallback the element emitted when the upstream errors
     * @return an error-tolerant stream
     */
    public StreamFlow<T> onErrorReturn(T fallback) {
        return of(new OnErrorReturnPublisher<>(publisher, fallback));
    }

    /**
     * Subscribes the given subscriber directly to the source publisher.
     *
     * @param subscriber the subscriber to attach
     */
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        publisher.subscribe(subscriber);
    }

    /**
     * Consumes every element and completes a future when the stream finishes.
     *
     * @param action the per-element action
     * @return a future that completes when the stream ends, or exceptionally on error
     */
    public CompletableFuture<Void> forEach(Consumer<? super T> action) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        subscribe(new TerminalSubscriber<T>() {
            @Override public void onNext(T item) {
                try {
                    action.accept(item);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
            @Override public void onError(Throwable t) { future.completeExceptionally(t); }
            @Override public void onComplete() { future.complete(null); }
        });
        return future;
    }

    /**
     * Collects every element into a list.
     *
     * @return a future that completes with all elements when the stream ends
     */
    public CompletableFuture<List<T>> toList() {
        CompletableFuture<List<T>> future = new CompletableFuture<>();
        List<T> buffer = new ArrayList<>();
        subscribe(new TerminalSubscriber<T>() {
            @Override public void onNext(T item) { buffer.add(item); }
            @Override public void onError(Throwable t) { future.completeExceptionally(t); }
            @Override public void onComplete() { future.complete(List.copyOf(buffer)); }
        });
        return future;
    }

    /**
     * Concatenates all stringified elements into a single string.
     *
     * @param delimiter the delimiter placed between elements
     * @return a future that completes with the joined string
     */
    public CompletableFuture<String> join(String delimiter) {
        return toList().thenApply(items -> items.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter)));
    }

    /**
     * Concatenates all stringified elements with no delimiter.
     *
     * @return a future that completes with the joined string
     */
    public CompletableFuture<String> join() {
        return join("");
    }

    /**
     * Counts the number of elements in the stream.
     *
     * @return a future that completes with the element count
     */
    public CompletableFuture<Long> count() {
        CompletableFuture<Long> future = new CompletableFuture<>();
        long[] counter = {0L};
        subscribe(new TerminalSubscriber<T>() {
            @Override public void onNext(T item) { counter[0]++; }
            @Override public void onError(Throwable t) { future.completeExceptionally(t); }
            @Override public void onComplete() { future.complete(counter[0]); }
        });
        return future;
    }

    /**
     * Blocks until the last element is emitted and returns it.
     *
     * <p>Unwraps {@link SynapseException}s so callers can handle provider
     * failures directly.</p>
     *
     * @return the last element, or {@code null} if the stream was empty
     * @throws SynapseException if the stream fails
     */
    public T blockLast() throws SynapseException {
        List<T> items = await(toList());
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    /**
     * Blocks until the first element is emitted and returns it.
     *
     * @return the first element, or {@code null} if the stream was empty
     * @throws SynapseException if the stream fails
     */
    public T blockFirst() throws SynapseException {
        List<T> items = await(toList());
        return items.isEmpty() ? null : items.get(0);
    }

    private static <R> R await(CompletableFuture<R> future) throws SynapseException {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SynapseException) {
                throw (SynapseException) cause;
            }
            throw new SynapseException("Stream failed", cause,
                    SynapseException.ExceptionType.STREAMING_ERROR);
        }
    }

    /** Minimal {@link Flow.Subscriber} that requests unbounded demand. */
    private abstract static class TerminalSubscriber<T> implements Flow.Subscriber<T> {
        @Override public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }
    }

    /**
     * Pass-through operator that applies a predicate or mapper on the way down.
     * Exactly one of {@code predicate} or {@code mapper} is non-null.
     */
    private static final class MapOrFilterPublisher<T, R> implements Flow.Publisher<R> {
        private final Flow.Publisher<T> source;
        private final Predicate<? super T> predicate;
        private final Function<? super T, ? extends R> mapper;

        MapOrFilterPublisher(Flow.Publisher<T> source, Predicate<? super T> predicate,
                             Function<? super T, ? extends R> mapper) {
            this.source = source;
            this.predicate = predicate;
            this.mapper = mapper;
        }

        @Override public void subscribe(Flow.Subscriber<? super R> downstream) {
            source.subscribe(new Flow.Subscriber<T>() {
                @Override public void onSubscribe(Flow.Subscription subscription) {
                    downstream.onSubscribe(new Flow.Subscription() {
                        @Override public void request(long n) { subscription.request(n); }
                        @Override public void cancel() { subscription.cancel(); }
                    });
                }

                @Override public void onNext(T item) {
                    try {
                        if (predicate != null && !predicate.test(item)) {
                            return;
                        }
                        @SuppressWarnings("unchecked")
                        R value = (R) (mapper != null ? mapper.apply(item) : item);
                        downstream.onNext(value);
                    } catch (Throwable t) {
                        downstream.onError(t);
                    }
                }

                @Override public void onError(Throwable throwable) { downstream.onError(throwable); }
                @Override public void onComplete() { downstream.onComplete(); }
            });
        }
    }

    /** Operator that replaces an upstream error with a fallback element. */
    private static final class OnErrorReturnPublisher<T> implements Flow.Publisher<T> {
        private final Flow.Publisher<T> source;
        private final T fallback;

        OnErrorReturnPublisher(Flow.Publisher<T> source, T fallback) {
            this.source = source;
            this.fallback = fallback;
        }

        @Override public void subscribe(Flow.Subscriber<? super T> downstream) {
            source.subscribe(new Flow.Subscriber<T>() {
                @Override public void onSubscribe(Flow.Subscription subscription) {
                    downstream.onSubscribe(new Flow.Subscription() {
                        @Override public void request(long n) { subscription.request(n); }
                        @Override public void cancel() { subscription.cancel(); }
                    });
                }

                @Override public void onNext(T item) { downstream.onNext(item); }

                @Override public void onError(Throwable throwable) {
                    downstream.onNext(fallback);
                    downstream.onComplete();
                }

                @Override public void onComplete() { downstream.onComplete(); }
            });
        }
    }
}
