package org.abhi.synapse.core;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.Model;
import org.abhi.synapse.core.model.SynapseResponse;
import org.abhi.synapse.core.exception.SynapseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * Main entry point for interacting with LLM services through Synapse.
 *
 * <p>Provides a unified API for synchronous, asynchronous, and streaming
 * interactions across multiple LLM providers. Every method that accepts
 * {@link RequestOptions} can be called with {@code null} to use config-level
 * defaults for all per-request overrides (model, temperature, maxTokens,
 * tools, responseFormat, timeouts).</p>
 *
 * <p>Streaming methods return a {@link StreamHandle} that provides both
 * a {@code cancel()} method for aborting mid-stream and a
 * {@link java.util.concurrent.CompletableFuture} that completes with the
 * aggregated response when the stream finishes.</p>
 *
 * <p>Usage examples:</p>
 * <pre>{@code
 * // One-shot with per-request options
 * RequestOptions opts = RequestOptions.defaults()
 *     .setModelName("gpt-4")
 *     .setTemperature(0.7);
 * SynapseResponse response = hub.sendPrompt("What is Java?", opts);
 *
 * // Multi-turn chat
 * List<ChatMessage> messages = List.of(
 *     ChatMessage.system("You are helpful"),
 *     ChatMessage.user("What is Java?")
 * );
 * SynapseResponse chatResponse = hub.sendChat(messages, null);
 *
 * // Streaming with cancellation
 * StreamHandle handle = hub.streamChat(messages, listener);
 * // Later: handle.cancel();
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see RequestOptions
 * @see StreamHandle
 * @see StreamListener
 */
public interface ISynapseHub {

    /**
     * Sends a single user prompt synchronously.
     *
     * @param prompt  the user's prompt text
     * @param options per-request overrides ({@code null} for config defaults)
     * @return the LLM response
     * @throws SynapseException if the request fails
     */
    SynapseResponse sendPrompt(String prompt, RequestOptions options) throws SynapseException;

    /**
     * Sends a multi-turn chat conversation synchronously.
     *
     * @param messages the conversation history
     * @param options  per-request overrides ({@code null} for config defaults)
     * @return the LLM response
     * @throws SynapseException if the request fails
     */
    SynapseResponse sendChat(List<ChatMessage> messages, RequestOptions options) throws SynapseException;

    /**
     * Sends a single user prompt asynchronously.
     *
     * @param prompt  the user's prompt text
     * @param options per-request overrides ({@code null} for config defaults)
     * @return a future that completes with the LLM response
     * @throws SynapseException if the request cannot be submitted
     */
    CompletableFuture<SynapseResponse> sendPromptAsync(String prompt, RequestOptions options) throws SynapseException;

    /**
     * Sends a multi-turn chat conversation asynchronously.
     *
     * @param messages the conversation history
     * @param options  per-request overrides ({@code null} for config defaults)
     * @return a future that completes with the LLM response
     * @throws SynapseException if the request cannot be submitted
     */
    CompletableFuture<SynapseResponse> sendChatAsync(List<ChatMessage> messages, RequestOptions options) throws SynapseException;

    /**
     * Sends a raw JSON request body synchronously (escape hatch).
     *
     * <p>Use when you need full control over the request body and the
     * structured methods are too restrictive. The body must be valid JSON
     * matching the provider's API contract.</p>
     *
     * @param requestBody the raw JSON request body
     * @param options     per-request overrides ({@code null} for config defaults)
     * @return the LLM response
     * @throws SynapseException if the request fails
     */
    SynapseResponse chatCompletion(String requestBody, RequestOptions options) throws SynapseException;

    /**
     * Streams a single user prompt with a {@link StreamListener} for
     * chunk-by-chunk, completion, and error callbacks.
     *
     * @param prompt   the user's prompt text
     * @param listener receives chunks, completion, and error signals
     * @return a handle that can cancel the stream or await the full response
     * @throws SynapseException if the request cannot be submitted
     */
    StreamHandle streamPrompt(String prompt, StreamListener listener) throws SynapseException;

    /**
     * Streams a multi-turn chat conversation with a {@link StreamListener}.
     *
     * @param messages the conversation history
     * @param listener receives chunks, completion, and error signals
     * @return a handle that can cancel the stream or await the full response
     * @throws SynapseException if the request cannot be submitted
     */
    StreamHandle streamChat(List<ChatMessage> messages, StreamListener listener) throws SynapseException;

    /**
     * Streams a raw JSON request body (escape hatch for streaming).
     *
     * @param requestBody the raw JSON request body
     * @param listener    receives chunks, completion, and error signals
     * @return a handle that can cancel the stream or await the full response
     * @throws SynapseException if the request cannot be submitted
     */
    StreamHandle streamCompletion(String requestBody, StreamListener listener) throws SynapseException;

    /**
     * Streams a multi-turn chat as a {@link Flow.Publisher} for reactive
     * consumption via {@link Flow.Subscriber}.
     *
     * @param messages the conversation history
     * @return a publisher of response text chunks
     * @throws SynapseException if the request cannot be submitted
     */
    Flow.Publisher<String> streamChatAsFlow(List<ChatMessage> messages) throws SynapseException;

    /**
     * Streams a single user prompt as a {@link Flow.Publisher}.
     *
     * @param prompt the user's prompt text
     * @return a publisher of response text chunks
     * @throws SynapseException if the request cannot be submitted
     */
    Flow.Publisher<String> streamPromptAsFlow(String prompt) throws SynapseException;

    /**
     * Lists all models available from the configured provider.
     *
     * @return the list of available models
     * @throws SynapseException if the request fails
     */
    List<Model> getModelsList() throws SynapseException;

    /**
     * Shuts down this hub, releasing all resources including the HTTP client
     * and async thread pool.
     */
    void close();
}
