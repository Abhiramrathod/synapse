package org.abhi.synapse.core.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageTest {

    @Test
    void withVariableRendersContent() {
        ChatMessage msg = ChatMessage.user("Hello {name}").withVariable("name", "Alice");
        assertThat(msg.getRole()).isEqualTo("user");
        assertThat(msg.getContent()).isEqualTo("Hello Alice");
    }

    @Test
    void withVariablesRendersContent() {
        ChatMessage msg = ChatMessage.user("Hello {name}, today is {day}.")
                .withVariables(Map.of("name", "Bob", "day", "Monday"));
        assertThat(msg.getContent()).isEqualTo("Hello Bob, today is Monday.");
    }

    @Test
    void withVariablesDoesNotMutateOriginal() {
        ChatMessage original = ChatMessage.user("Hello {name}");
        ChatMessage rendered = original.withVariable("name", "Carol");
        assertThat(original.getContent()).isEqualTo("Hello {name}");
        assertThat(rendered.getContent()).isEqualTo("Hello Carol");
    }

    @Test
    void withVariablesWithoutPlaceholdersReturnsSameInstance() {
        ChatMessage msg = ChatMessage.user("Plain text");
        assertThat(msg.withVariables(Map.of("name", "Alice"))).isSameAs(msg);
    }

    @Test
    void withUnknownVariableReturnsSameInstance() {
        ChatMessage msg = ChatMessage.user("Hello {name}");
        assertThat(msg.withVariables(Map.of("other", "x"))).isSameAs(msg);
    }

    @Test
    void withVariableOnNullContentIsSafe() {
        ChatMessage msg = new ChatMessage("assistant", null);
        assertThat(msg.withVariable("name", "Alice")).isSameAs(msg);
    }

    @Test
    void withVariableCopiesToolFields() {
        ChatMessage msg = ChatMessage.tool("call_1", "calculator", "result {value}")
                .withVariable("value", "42");
        assertThat(msg.getRole()).isEqualTo("tool");
        assertThat(msg.getToolCallId()).isEqualTo("call_1");
        assertThat(msg.getName()).isEqualTo("calculator");
        assertThat(msg.getContent()).isEqualTo("result 42");
    }
}
