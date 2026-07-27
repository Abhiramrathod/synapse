package org.abhi.synapse.core;

import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class StreamListenerTest {

    @Test
    void ofAdaptsConsumerToStreamListener() {
        List<String> received = new ArrayList<>();
        StreamListener listener = StreamListener.of(received::add);

        listener.onChunk("hello");
        listener.onChunk(" world");

        assertThat(received).containsExactly("hello", " world");
    }

    @Test
    void ofOnCompleteIsNoOp() {
        StreamListener listener = StreamListener.of(s -> {});
        listener.onComplete(new SynapseResponse("done", "gpt-4", 10, 20, "stop"));
    }

    @Test
    void ofOnErrorIsNoOp() {
        StreamListener listener = StreamListener.of(s -> {});
        listener.onError(new org.abhi.synapse.core.exception.SynapseException("test"));
    }
}
