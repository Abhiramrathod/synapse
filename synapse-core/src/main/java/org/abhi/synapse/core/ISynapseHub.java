package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.exception.SynapseException;

import java.util.List;
import java.util.function.Consumer;

/**
 * Main interface for interacting with Large Language Model (LLM) services.
 *
 * <p>{@code ISynapseHub} provides a unified abstraction layer for sending prompts,
 * managing chat conversations, and performing chat completions against LLM providers.
 * It supports both synchronous (blocking) and asynchronous (streaming) communication
 * patterns, making it suitable for a wide range of application architectures.</p>
 *
 * <p>Implementations of this interface handle the underlying HTTP communication,
 * authentication, retry logic, and response parsing, allowing consumers to focus
 * on prompt engineering and response processing.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Synchronous prompt
 * ISynapseHub hub = synapseFactory.createHub(config);
 * SynapseResponse response = hub.sendPrompt("Explain quantum computing");
 * System.out.println(response.getContent());
 *
 * // Chat conversation
 * List<ChatMessage> messages = List.of(
 *     ChatMessage.system("You are a helpful assistant"),
 *     ChatMessage.user("What is Java?")
 * );
 * SynapseResponse chatResponse = hub.sendChat(messages);
 *
 * // Streaming
 * hub.streamPrompt("Write a poem", chunk -> System.out.print(chunk));
 *
 * hub.close();
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see SynapseResponse
 * @see ChatMessage
 * @see SynapseException
 */
public interface ISynapseHub {

    /**
     * Sends a single prompt to the LLM and returns the complete response synchronously.
     *
     * <p>This is a convenience method that wraps the prompt in a user message and sends
     * it as a single-turn conversation. The method blocks until the full response is received.</p>
     *
     * @param prompt the text prompt to send to the LLM
     * @return a {@link SynapseResponse} containing the generated content, model information,
     *         and token usage statistics
     * @throws SynapseException if the request fails due to network issues, rate limiting,
     *         server errors, or configuration problems
     * @since 1.0.0
     */
    SynapseResponse sendPrompt(String prompt) throws SynapseException;

    /**
     * Sends a multi-turn chat conversation to the LLM and returns the complete response synchronously.
     *
     * <p>This method supports full conversation history, allowing the LLM to maintain context
     * across multiple exchanges. The conversation typically starts with a system message followed
     * by alternating user and assistant messages.</p>
     *
     * @param messages the ordered list of {@link ChatMessage} objects representing the conversation
     * @return a {@link SynapseResponse} containing the assistant's reply, model information,
     *         and token usage statistics
     * @throws SynapseException if the request fails due to network issues, rate limiting,
     *         server errors, or configuration problems
     * @throws IllegalArgumentException if the messages list is null or empty
     * @since 1.0.0
     * @see ChatMessage#system(String)
     * @see ChatMessage#user(String)
     */
    SynapseResponse sendChat(List<ChatMessage> messages) throws SynapseException;

    /**
     * Sends a raw chat completion request body to the LLM and returns the complete response synchronously.
     *
     * <p>This method provides low-level access to the chat completion API, allowing callers to
     * construct custom request payloads with advanced parameters such as function calling,
     * response format constraints, or provider-specific options.</p>
     *
     * @param requestBody a JSON string containing the complete request body to send
     * @return a {@link SynapseResponse} containing the generated content, model information,
     *         and token usage statistics
     * @throws SynapseException if the request fails due to network issues, rate limiting,
     *         server errors, configuration problems, or malformed JSON
     * @since 1.0.0
     */
    SynapseResponse chatCompletion(String requestBody) throws SynapseException;

    /**
     * Sends a prompt to the LLM and streams the response chunks asynchronously via a callback.
     *
     * <p>Unlike {@link #sendPrompt(String)}, this method does not block waiting for the full
     * response. Instead, each text chunk is delivered to the provided {@link Consumer} as it
     * becomes available from the LLM, enabling real-time display of generated text.</p>
     *
     * @param prompt the text prompt to send to the LLM
     * @param onChunk a {@link Consumer} callback that receives each text chunk as it arrives;
     *                may be called multiple times before this method returns
     * @throws SynapseException if the request fails due to network issues, rate limiting,
     *         server errors, or configuration problems
     * @since 1.0.0
     */
    void streamPrompt(String prompt, Consumer<String> onChunk) throws SynapseException;

    /**
     * Sends a multi-turn chat conversation to the LLM and streams the response chunks
     * asynchronously via a callback.
     *
     * <p>This method combines the multi-turn capability of {@link #sendChat(List)} with
     * the streaming behavior of {@link #streamPrompt(String, Consumer)}, allowing
     * real-time delivery of the assistant's response during a conversation.</p>
     *
     * @param messages the ordered list of {@link ChatMessage} objects representing the conversation
     * @param onChunk a {@link Consumer} callback that receives each text chunk as it arrives;
     *                may be called multiple times before this method returns
     * @throws SynapseException if the request fails due to network issues, rate limiting,
     *         server errors, or configuration problems
     * @throws IllegalArgumentException if the messages list is null or empty
     * @since 1.0.0
     */
    void streamChat(List<ChatMessage> messages, Consumer<String> onChunk) throws SynapseException;

    /**
     * Sends a raw chat completion request body to the LLM and streams the response chunks
     * asynchronously via a callback.
     *
     * <p>This method provides low-level streaming access to the chat completion API, similar
     * to {@link #chatCompletion(String)} but with real-time chunk delivery.</p>
     *
     * @param requestBody a JSON string containing the complete request body to send
     * @param onChunk a {@link Consumer} callback that receives each text chunk as it arrives;
     *                may be called multiple times before this method returns
     * @throws SynapseException if the request fails due to network issues, rate limiting,
     *         server errors, configuration problems, or malformed JSON
     * @since 1.0.0
     */
    void streamCompletion(String requestBody, Consumer<String> onChunk) throws SynapseException;


    List<Model> getModelsList();

    /**
     * Releases any resources held by this hub, including HTTP connections and threads.
     *
     * <p>After calling this method, the hub instance should not be reused. Implementations
     * should ensure that any pending streaming operations are gracefully completed or
     * cancelled before resources are released.</p>
     *
     * @since 1.0.0
     */
    void close();
}
