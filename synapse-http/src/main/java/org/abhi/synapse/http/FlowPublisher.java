package org.abhi.synapse.http;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

public class FlowPublisher<T> implements Flow.Publisher<T> {
    private final List<Flow.Subscriber<? super T>> subscribers = new CopyOnWriteArrayList<>();
    @Override public void subscribe(Flow.Subscriber<? super T> subscriber) {
        FlowSub sub = new FlowSub(subscriber); subscribers.add(subscriber); subscriber.onSubscribe(sub);
    }
    void submit(T item) { for (Flow.Subscriber<? super T> s : subscribers) s.onNext(item); }
    void close() { for (Flow.Subscriber<? super T> s : subscribers) s.onComplete(); }
    void fail(Throwable t) { for (Flow.Subscriber<? super T> s : subscribers) s.onError(t); }
    private static class FlowSub implements Flow.Subscription {
        private final Flow.Subscriber<?> sub;
        FlowSub(Flow.Subscriber<?> sub) { this.sub = sub; }
        @Override public void request(long n) {}
        @Override public void cancel() {}
    }
}
