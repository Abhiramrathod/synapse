package org.abhi.synapse.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolCallTest {

    @Test
    void toolCallCreation() {
        ToolCall tc = new ToolCall("call_1", "function", "get_weather", "{\"city\":\"NYC\"}");
        assertThat(tc.getId()).isEqualTo("call_1");
        assertThat(tc.getType()).isEqualTo("function");
        assertThat(tc.getFunction()).isEqualTo("get_weather");
        assertThat(tc.getArguments()).isEqualTo("{\"city\":\"NYC\"}");
    }

    @Test
    void toolCallSetters() {
        ToolCall tc = new ToolCall();
        tc.setId("call_2");
        tc.setType("function");
        tc.setFunction("search");
        tc.setArguments("{}");

        assertThat(tc.getId()).isEqualTo("call_2");
        assertThat(tc.getFunction()).isEqualTo("search");
    }
}
