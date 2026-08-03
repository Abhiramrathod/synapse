package org.abhi.synapse.test;

import org.abhi.synapse.core.LoadBalancingSynapseHub;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoadBalancingSynapseHubTest {

    @Test
    void roundRobinsSynchronousCallsAcrossHubs() throws SynapseException {
        RecordingHub a = new RecordingHub("A");
        RecordingHub b = new RecordingHub("B");
        LoadBalancingSynapseHub hub = new LoadBalancingSynapseHub(a, b);

        assertThat(hub.sendPrompt("1", null).getContent()).isEqualTo("A");
        assertThat(hub.sendPrompt("2", null).getContent()).isEqualTo("B");
        assertThat(hub.sendPrompt("3", null).getContent()).isEqualTo("A");
        assertThat(hub.sendPrompt("4", null).getContent()).isEqualTo("B");
    }

    @Test
    void asyncCallsAreRoutedRoundRobin() throws Exception {
        RecordingHub a = new RecordingHub("A");
        RecordingHub b = new RecordingHub("B");
        LoadBalancingSynapseHub hub = new LoadBalancingSynapseHub(a, b);

        CompletableFuture<SynapseResponse> first = hub.sendPromptAsync("1", null);
        CompletableFuture<SynapseResponse> second = hub.sendPromptAsync("2", null);

        assertThat(first.join().getContent()).isEqualTo("A");
        assertThat(second.join().getContent()).isEqualTo("B");
    }

    @Test
    void streamingIsRoutedToNextHub() throws SynapseException {
        RecordingHub a = new RecordingHub("A");
        RecordingHub b = new RecordingHub("B");
        LoadBalancingSynapseHub hub = new LoadBalancingSynapseHub(a, b);

        hub.streamChat(List.of(ChatMessage.user("hi")), StreamListener.of(chunk -> { }));

        assertThat(a.streamCount.get()).isEqualTo(1);
        assertThat(b.streamCount.get()).isZero();
    }

    @Test
    void failedHubPropagatesError() {
        RecordingHub failing = new RecordingHub(new SynapseException("down",
                SynapseException.ExceptionType.SERVER_ERROR));
        RecordingHub ok = new RecordingHub("OK");
        LoadBalancingSynapseHub hub = new LoadBalancingSynapseHub(failing, ok);

        assertThatThrownBy(() -> hub.sendPrompt("x", null)).hasMessage("down");
    }

    @Test
    void closeClosesEveryHub() {
        RecordingHub a = new RecordingHub("A");
        RecordingHub b = new RecordingHub("B");
        LoadBalancingSynapseHub hub = new LoadBalancingSynapseHub(a, b);

        hub.close();

        assertThat(a.closeCount.get()).isEqualTo(1);
        assertThat(b.closeCount.get()).isEqualTo(1);
    }

    @Test
    void requiresAtLeastOneHub() {
        assertThatThrownBy(() -> new LoadBalancingSynapseHub(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
