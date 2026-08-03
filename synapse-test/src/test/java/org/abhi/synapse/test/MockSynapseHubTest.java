package org.abhi.synapse.test;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockSynapseHubTest {

    private final MockSynapseHub hub = new MockSynapseHub();

    @Test
    void sendPromptReturnsStubbedContent() throws SynapseException {
        hub.stubResponse("Hello from mock!");

        SynapseResponse response = hub.sendPrompt("Hi", null);

        assertThat(response.getContent()).isEqualTo("Hello from mock!");
    }

    @Test
    void withResponseFactoryReturnsReadyHub() throws SynapseException {
        MockSynapseHub ready = MockSynapseHub.withResponse("pre-stubbed");

        assertThat(ready.sendPrompt("x", null).getContent()).isEqualTo("pre-stubbed");
    }

    @Test
    void stubErrorIsThrown() {
        hub.stubError(new SynapseException("boom", SynapseException.ExceptionType.SERVER_ERROR));

        assertThatThrownBy(() -> hub.sendPrompt("Hi", null))
                .isInstanceOf(SynapseException.class)
                .hasMessage("boom");
    }

    @Test
    void typedSendPromptParsesStubbedJson() throws SynapseException {
        hub.stubResponse("{\"name\":\"Ada\",\"age\":36}");

        Person person = hub.sendPrompt("Who?", Person.class, null);

        assertThat(person.name).isEqualTo("Ada");
        assertThat(person.age).isEqualTo(36);
    }

    @Test
    void sendChatRecordsMessages() throws SynapseException {
        hub.stubResponse("Sure");
        List<ChatMessage> messages = List.of(ChatMessage.system("be nice"), ChatMessage.user("help"));

        hub.sendChat(messages, null);

        assertThat(hub.recordedChats()).containsExactly(messages);
        assertThat(hub.recordedChats().get(0).get(1).getContent()).isEqualTo("help");
    }

    @Test
    void asyncPromptCompletesWithStubbedContent() throws Exception {
        hub.stubResponse("async!");

        CompletableFuture<SynapseResponse> future = hub.sendPromptAsync("Hi", null);

        assertThat(future.get(5, TimeUnit.SECONDS).getContent()).isEqualTo("async!");
        assertThat(hub.callCount()).isEqualTo(1);
    }

    @Test
    void asyncPromptWithStubbedErrorCompletesExceptionally() throws Exception {
        hub.stubError(new SynapseException("nope", SynapseException.ExceptionType.SERVER_ERROR));

        CompletableFuture<SynapseResponse> future = hub.sendPromptAsync("Hi", null);

        assertThat(future.isCompletedExceptionally()).isTrue();
    }

    @Test
    void chatCompletionRecordsRawBody() throws SynapseException {
        hub.stubResponse("ok");

        hub.chatCompletion("{\"model\":\"gpt-4\"}", null);

        assertThat(hub.recordedBodies()).containsExactly("{\"model\":\"gpt-4\"}");
    }

    @Test
    void streamingEmitsChunksAndAggregates() throws SynapseException {
        hub.stubStreaming(List.of("Hel", "lo ", "world"));
        List<String> received = new CopyOnWriteArrayList<>();
        AtomicReference<SynapseResponse> complete = new AtomicReference<>();

        StreamHandle handle = hub.streamPrompt("Hi", new StreamListener() {
            @Override public void onChunk(String text) { received.add(text); }
            @Override public void onComplete(SynapseResponse fullResponse) { complete.set(fullResponse); }
            @Override public void onError(SynapseException error) { }
        });

        assertThat(received).containsExactly("Hel", "lo ", "world");
        assertThat(complete.get().getContent()).isEqualTo("Hello world");
        assertThat(handle.getFuture().join().getContent()).isEqualTo("Hello world");
    }

    @Test
    void streamingWithStubbedErrorInvokesOnError() throws Exception {
        hub.stubError(new SynapseException("stream down", SynapseException.ExceptionType.STREAMING_ERROR));
        AtomicReference<SynapseException> error = new AtomicReference<>();

        StreamHandle handle = hub.streamPrompt("Hi", new StreamListener() {
            @Override public void onChunk(String text) { }
            @Override public void onComplete(SynapseResponse fullResponse) { }
            @Override public void onError(SynapseException e) { error.set(e); }
        });

        assertThat(error.get()).hasMessage("stream down");
        assertThat(handle.getFuture().isCompletedExceptionally()).isTrue();
    }

    @Test
    void streamChatAsFlowEmitsChunks() throws Exception {
        hub.stubStreaming(List.of("a", "b"));
        List<String> received = new CopyOnWriteArrayList<>();

        CompletableFuture<Void> done = new CompletableFuture<>();
        hub.streamChatAsFlow(List.of(ChatMessage.user("hey"))).subscribe(new java.util.concurrent.Flow.Subscriber<>() {
            @Override public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }
            @Override public void onNext(String item) { received.add(item); }
            @Override public void onError(Throwable throwable) { done.completeExceptionally(throwable); }
            @Override public void onComplete() { done.complete(null); }
        });

        done.get(5, TimeUnit.SECONDS);
        assertThat(received).containsExactly("a", "b");
    }

    @Test
    void getModelsListReturnsStubbedModels() throws SynapseException {
        hub.stubModels(List.of(Model.builder().id("gpt-4").ownedBy("openai").build()));

        assertThat(hub.getModelsList()).extracting(Model::getId).containsExactly("gpt-4");
    }

    @Test
    void verificationAndReset() throws SynapseException {
        hub.stubResponse("one");
        hub.sendPrompt("first", null);
        hub.sendPrompt("second", null);

        assertThat(hub.recordedPrompts()).containsExactly("first", "second");
        hub.verifyCalled(2);

        assertThatThrownBy(() -> hub.verifyCalled(1)).isInstanceOf(AssertionError.class);

        hub.reset();
        assertThat(hub.callCount()).isZero();
        assertThat(hub.recordedPrompts()).isEmpty();
    }

    public static final class Person {
        public String name;
        public int age;
    }
}
