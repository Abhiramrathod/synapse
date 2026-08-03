package org.abhi.synapse.http;

import org.abhi.synapse.core.model.ChatMessage;
import org.abhi.synapse.core.model.ToolCall;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * Configures an {@link ObjectMapper} so that the core chat models serialize in
 * the OpenAI-compatible wire format.
 *
 * <p>The core module is intentionally free of Jackson dependencies, so the
 * snake_case field names ({@code tool_call_id}, {@code tool_calls}) and the
 * nested {@code function} object of {@link ToolCall} are applied here via
 * mix-ins and a custom serializer.</p>
 */
final class SynapseJson {

    private SynapseJson() {
    }

    /**
     * Returns an {@link ObjectMapper} with the Synapse wire-format mix-ins applied.
     * The supplied mapper is mutated in place and also returned.
     */
    static ObjectMapper configure(ObjectMapper objectMapper) {
        objectMapper.addMixIn(ChatMessage.class, ChatMessageMixin.class);
        objectMapper.addMixIn(ToolCall.class, ToolCallMixin.class);
        return objectMapper;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    abstract static class ChatMessageMixin {
        @JsonProperty("tool_call_id")
        abstract String getToolCallId();

        @JsonProperty("tool_calls")
        abstract java.util.List<ToolCall> getToolCalls();
    }

    @JsonSerialize(using = ToolCallSerializer.class)
    abstract static class ToolCallMixin {
    }

    static final class ToolCallSerializer extends JsonSerializer<ToolCall> {
        @Override
        public void serialize(ToolCall value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();
            if (value.getId() != null) gen.writeStringField("id", value.getId());
            if (value.getType() != null) gen.writeStringField("type", value.getType());
            gen.writeObjectFieldStart("function");
            if (value.getFunction() != null) gen.writeStringField("name", value.getFunction());
            if (value.getArguments() != null) gen.writeStringField("arguments", value.getArguments());
            gen.writeEndObject();
            gen.writeEndObject();
        }
    }
}
