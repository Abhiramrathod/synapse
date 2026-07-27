package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.exception.SynapseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

public interface ISynapseHub {

    SynapseResponse sendPrompt(String prompt) throws SynapseException;
    SynapseResponse sendPrompt(String prompt, String modelName) throws SynapseException;
    SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException;

    CompletableFuture<SynapseResponse> sendPromptAsync(String prompt) throws SynapseException;
    CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException;

    SynapseResponse sendChat(List<ChatMessage> messages) throws SynapseException;
    SynapseResponse sendChat(List<ChatMessage> messages, String modelName) throws SynapseException;
    SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException;

    CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages) throws SynapseException;
    CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException;

    SynapseResponse chatCompletion(String requestBody) throws SynapseException;
    SynapseResponse chatCompletion(String requestBody, String modelName) throws SynapseException;

    void streamPrompt(String prompt, Consumer<String> onChunk) throws SynapseException;
    void streamPrompt(String prompt, Consumer<String> onChunk, String modelName) throws SynapseException;
    StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException;

    void streamChat(List<ChatMessage> messages, Consumer<String> onChunk) throws SynapseException;
    void streamChat(List<ChatMessage> messages, Consumer<String> onChunk, String modelName) throws SynapseException;
    StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException;

    void streamCompletion(String requestBody, Consumer<String> onChunk) throws SynapseException;
    void streamCompletion(String requestBody, Consumer<String> onChunk, String modelName) throws SynapseException;
    StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException;

    Flow.Publisher<String> streamCompletionAsFlow(String requestBody) throws SynapseException;
    Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException;

    List<Model> getModelsList();
    void close();
}
