package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.exception.SynapseException;

import java.util.List;
import java.util.function.Consumer;

public interface ISynapseHub {

    SynapseResponse sendPrompt(String prompt) throws SynapseException;

    SynapseResponse sendChat(List<ChatMessage> messages) throws SynapseException;

    SynapseResponse chatCompletion(String requestBody) throws SynapseException;

    void streamPrompt(String prompt, Consumer<String> onChunk) throws SynapseException;

    void streamChat(List<ChatMessage> messages, Consumer<String> onChunk) throws SynapseException;

    void streamCompletion(String requestBody, Consumer<String> onChunk) throws SynapseException;

    void close();
}
