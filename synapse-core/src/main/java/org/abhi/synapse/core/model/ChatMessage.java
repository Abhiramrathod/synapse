package org.abhi.synapse.core.model;

import java.util.List;

/**
 * Represents a single message in a chat conversation with an LLM.
 *
 * <p>{@code ChatMessage} encapsulates a message exchanged between participants in a
 * multi-turn conversation. Each message has a {@code role} (identifying the speaker)
 * and {@code content} (the text of the message). This model follows the standard
 * OpenAI-compatible chat message format used by most LLM providers.</p>
 *
 * <p>Static factory methods are provided for convenient creation of common message types
 * ({@code system}, {@code user}, {@code assistant}), promoting readable and expressive
 * conversation construction.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * List<ChatMessage> messages = List.of(
 *     ChatMessage.system("You are a helpful coding assistant."),
 *     ChatMessage.user("Write a Java hello world program"),
 *     ChatMessage.assistant("Here's a simple Java program:"),
 *     ChatMessage.user("Now add error handling")
 * );
 *
 * SynapseResponse response = hub.sendChat(messages);
 * }</pre>
 *
 * @author Abhiram Rathod
 * @since 1.0.0
 * @see org.abhi.synapse.core.ISynapseHub#sendChat(java.util.List)
 * @see org.abhi.synapse.core.ISynapseHub#streamChat(java.util.List, java.util.function.Consumer)
 */
public class ChatMessage {

    private String role;
    private String content;
    private String toolCallId;
    private String name;
    private List<ToolCall> toolCalls;

    /**
     * Default no-argument constructor for {@code ChatMessage}.
     *
     * <p>Primarily intended for use with JSON serialization/deserialization frameworks.
     * Prefer using the parameterized constructor or static factory methods for programmatic creation.</p>
     *
     * @since 1.0.0
     */
    public ChatMessage() {
    }

    /**
     * Constructs a new {@code ChatMessage} with the specified role and content.
     *
     * @param role    the role of the message sender (e.g., {@code "system"}, {@code "user"}, {@code "assistant"})
     * @param content the text content of the message
     * @since 1.0.0
     */
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * Returns the role identifying the message sender.
     *
     * @return the message role
     * @since 1.0.0
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role identifying the message sender.
     *
     * @param role the message role (e.g., {@code "system"}, {@code "user"}, {@code "assistant"})
     * @since 1.0.0
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the text content of this message.
     *
     * @return the message content
     * @since 1.0.0
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the text content of this message.
     *
     * @param content the message content
     * @since 1.0.0
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the tool call ID this message is responding to.
     *
     * <p>Used when {@code role} is {@code "tool"} to identify which tool call this
     * response corresponds to.</p>
     *
     * @return the tool call ID, or {@code null} if not set
     * @since 1.0.0
     */
    public String getToolCallId() {
        return toolCallId;
    }

    /**
     * Sets the tool call ID this message is responding to.
     *
     * @param toolCallId the tool call ID
     * @since 1.0.0
     */
    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    /**
     * Returns the name associated with this message.
     *
     * <p>Used for tool messages to identify the tool that produced the result.</p>
     *
     * @return the name, or {@code null} if not set
     * @since 1.0.0
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name associated with this message.
     *
     * @param name the name
     * @since 1.0.0
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the tool calls made by the assistant in this message.
     *
     * <p>Used when {@code role} is {@code "assistant"} and the model requests tool
     * invocations rather than generating text content.</p>
     *
     * @return the list of tool calls, or {@code null} if not set
     * @since 1.0.0
     */
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }

    /**
     * Sets the tool calls made by the assistant in this message.
     *
     * @param toolCalls the list of tool calls
     * @since 1.0.0
     */
    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    /**
     * Creates a system message that sets the behavior and persona for the assistant.
     *
     * <p>System messages are typically placed at the beginning of a conversation to
     * establish the assistant's role, constraints, and personality.</p>
     *
     * @param content the system instruction text
     * @return a new {@code ChatMessage} with role {@code "system"}
     * @since 1.0.0
     */
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    /**
     * Creates a user message representing input from the human participant.
     *
     * @param content the user's message text
     * @return a new {@code ChatMessage} with role {@code "user"}
     * @since 1.0.0
     */
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    /**
     * Creates an assistant message representing a previous response from the LLM.
     *
     * <p>This is useful for constructing conversation history when providing context
     * from prior exchanges.</p>
     *
     * @param content the assistant's response text
     * @return a new {@code ChatMessage} with role {@code "assistant"}
     * @since 1.0.0
     */
    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }

    /**
     * Creates a tool result message containing the output of a tool invocation.
     *
     * <p>This is used in multi-turn tool-calling conversations to provide the LLM
     * with the result of a previously requested tool call.</p>
     *
     * @param toolCallId the ID of the tool call this message responds to
     * @param name       the name of the tool that was invoked
     * @param content    the result content produced by the tool
     * @return a new {@code ChatMessage} with role {@code "tool"}
     * @since 1.0.0
     */
    public static ChatMessage tool(String toolCallId, String name, String content) {
        ChatMessage msg = new ChatMessage("tool", content);
        msg.setToolCallId(toolCallId);
        msg.setName(name);
        return msg;
    }
}
