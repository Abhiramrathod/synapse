package org.abhi.synapse.test;

import org.abhi.synapse.core.FallbackSynapseHub;
import org.abhi.synapse.core.RequestOptions;
import org.abhi.synapse.core.StreamHandle;
import org.abhi.synapse.core.StreamListener;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FallbackSynapseHubTest {

    private static final SynapseException PROVIDER_DOWN = new SynapseException("provider down",
            SynapseException.ExceptionType.SERVER_ERROR);

    @Test
    void firstHealthyHubServesTheCall() throws SynapseException {
        RecordingHub primary = new RecordingHub("primary");
        RecordingHub backup = new RecordingHub("backup");
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        SynapseResponse response = hub.sendPrompt("hi", null);

        assertThat(response.getContent()).isEqualTo("primary");
        assertThat(primary.delegate.callCount()).isEqualTo(1);
        assertThat(backup.delegate.callCount()).isZero();
    }

    @Test
    void fallsBackWhenPrimaryFails() throws SynapseException {
        RecordingHub primary = new RecordingHub(PROVIDER_DOWN);
        RecordingHub backup = new RecordingHub("backup");
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        SynapseResponse response = hub.sendPrompt("hi", null);

        assertThat(response.getContent()).isEqualTo("backup");
        assertThat(primary.delegate.callCount()).isEqualTo(1);
        assertThat(backup.delegate.callCount()).isEqualTo(1);
    }

    @Test
    void throwsLastErrorWhenEveryHubFails() {
        FallbackSynapseHub hub = new FallbackSynapseHub(
                new RecordingHub(PROVIDER_DOWN),
                new RecordingHub(new SynapseException("also down", SynapseException.ExceptionType.NETWORK_ERROR)));

        assertThatThrownBy(() -> hub.sendPrompt("hi", null))
                .isInstanceOf(SynapseException.class)
                .hasMessage("also down");
    }

    @Test
    void asyncFallsBackToNextHub() throws Exception {
        RecordingHub primary = new RecordingHub(PROVIDER_DOWN);
        RecordingHub backup = new RecordingHub("backup");
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        SynapseResponse response = hub.sendPromptAsync("hi", null).get(5, TimeUnit.SECONDS);

        assertThat(response.getContent()).isEqualTo("backup");
    }

    @Test
    void asyncCompletesExceptionallyWhenEveryHubFails() {
        FallbackSynapseHub hub = new FallbackSynapseHub(
                new RecordingHub(PROVIDER_DOWN),
                new RecordingHub(PROVIDER_DOWN));

        CompletableFuture<SynapseResponse> future = hub.sendPromptAsync("hi", null);

        assertThat(future.isCompletedExceptionally()).isTrue();
    }

    @Test
    void typedPromptFallsBackAndParses() throws SynapseException {
        RecordingHub primary = new RecordingHub(PROVIDER_DOWN);
        RecordingHub backup = new RecordingHub("{\"name\":\"Ada\"}");
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        Person person = hub.sendPrompt("who?", Person.class, null);

        assertThat(person.name).isEqualTo("Ada");
    }

    @Test
    void chatCompletionFallsBack() throws SynapseException {
        RecordingHub primary = new RecordingHub(PROVIDER_DOWN);
        RecordingHub backup = new RecordingHub("ok");
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        SynapseResponse response = hub.chatCompletion("{}", null);

        assertThat(response.getContent()).isEqualTo("ok");
    }

    @Test
    void streamingSubmitsToFirstAvailableHub() throws SynapseException {
        FailingSubmitHub primary = new FailingSubmitHub();
        RecordingHub backup = new RecordingHub("streamed");
        backup.delegate.stubStreaming(List.of("a", "b"));
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        List<String> received = new java.util.concurrent.CopyOnWriteArrayList<>();
        AtomicReference<SynapseResponse> complete = new AtomicReference<>();
        hub.streamPrompt("hi", new StreamListener() {
            @Override public void onChunk(String text) { received.add(text); }
            @Override public void onComplete(SynapseResponse fullResponse) { complete.set(fullResponse); }
            @Override public void onError(SynapseException error) { }
        });

        assertThat(received).containsExactly("a", "b");
        assertThat(complete.get().getContent()).isEqualTo("ab");
    }

    @Test
    void modelsListFallsBack() throws SynapseException {
        RecordingHub backup = new RecordingHub("");
        backup.delegate.stubModels(List.of(org.abhi.synapse.core.model.Model.builder().id("gpt-4").build()));
        FallbackSynapseHub hub = new FallbackSynapseHub(new FailingSubmitHub(), backup);

        assertThat(hub.getModelsList()).extracting(Model::getId).containsExactly("gpt-4");
    }

    @Test
    void closeClosesEveryHub() {
        RecordingHub primary = new RecordingHub("a");
        RecordingHub backup = new RecordingHub("b");
        FallbackSynapseHub hub = new FallbackSynapseHub(primary, backup);

        hub.close();

        assertThat(primary.closeCount.get()).isEqualTo(1);
        assertThat(backup.closeCount.get()).isEqualTo(1);
    }

    public static final class Person {
        public String name;
    }

    private static final class FailingSubmitHub implements org.abhi.synapse.core.ISynapseHub {
        @Override public SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException {
            return CompletableFuture.failedFuture(PROVIDER_DOWN);
        }
        @Override public CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException {
            return CompletableFuture.failedFuture(PROVIDER_DOWN);
        }
        @Override public SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public java.util.concurrent.Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public java.util.concurrent.Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public List<Model> getModelsList() throws SynapseException {
            throw PROVIDER_DOWN;
        }
        @Override public void close() { }
    }
}
