package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ToolCall;
import org.abhi.synapse.core.model.ToolDefinition;
import org.abhi.synapse.core.tool.SynapseTool;
import org.abhi.synapse.core.tool.ToolParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Discovers, describes and invokes declarative tools annotated with
 * {@link SynapseTool}.
 *
 * <p>Constructed from a list of tool instances. Each instance's public methods
 * are scanned: a method is exposed as a tool when the method itself or its
 * declaring class is annotated with {@link SynapseTool}. The method's parameter
 * list is turned into a JSON Schema via {@link JsonSchemaGenerator}, and the
 * model-supplied arguments are bound back to the method parameters before the
 * method is invoked reflectively.</p>
 */
final class ToolRegistry {

    private final ObjectMapper objectMapper;
    private final List<ToolEntry> entries = new ArrayList<>();

    ToolRegistry(ObjectMapper objectMapper, List<Object> instances) {
        this.objectMapper = objectMapper;
        for (Object instance : instances) {
            register(instance);
        }
    }

    private void register(Object instance) {
        Class<?> type = instance.getClass();
        boolean classAnnotated = type.isAnnotationPresent(SynapseTool.class);
        SynapseTool classAnnotation = classAnnotated ? type.getAnnotation(SynapseTool.class) : null;

        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class) continue;
            boolean methodAnnotated = method.isAnnotationPresent(SynapseTool.class);
            if (!classAnnotated && !methodAnnotated) continue;

            SynapseTool annotation = methodAnnotated ? method.getAnnotation(SynapseTool.class) : classAnnotation;
            String name = !annotation.name().isEmpty() ? annotation.name() : method.getName();
            entries.add(new ToolEntry(name, annotation.description(), instance, method));
        }
    }

    /**
     * Returns the {@link ToolDefinition}s to advertise to the model.
     */
    List<ToolDefinition> definitions() {
        List<ToolDefinition> definitions = new ArrayList<>(entries.size());
        for (ToolEntry entry : entries) {
            try {
                String schema = objectMapper.writeValueAsString(
                        JsonSchemaGenerator.generateSchema(entry.method.getParameters(), objectMapper));
                definitions.add(ToolDefinition.of(entry.name, entry.description, schema));
            } catch (Exception e) {
                throw new SynapseException("Failed to build schema for tool '" + entry.name + "'", e,
                        SynapseException.ExceptionType.TOOL_ERROR);
            }
        }
        return definitions;
    }

    /**
     * Invokes the tool with the given name, binding the JSON arguments to the
     * method parameters. Returns the JSON-serialized result (raw for String results).
     *
     * @throws SynapseException with type {@link SynapseException.ExceptionType#TOOL_ERROR}
     *                          when the tool is unknown or invocation fails
     */
    String invoke(String name, String argumentsJson) throws SynapseException {
        ToolEntry entry = find(name);
        if (entry == null) {
            throw new SynapseException("No tool registered under name '" + name + "'",
                    SynapseException.ExceptionType.TOOL_ERROR);
        }
        try {
            JsonNode arguments = argumentsJson == null || argumentsJson.isBlank()
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(argumentsJson);

            Parameter[] parameters = entry.method.getParameters();
            Object[] values = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
                String paramName = toolParam != null && !toolParam.name().isEmpty()
                        ? toolParam.name()
                        : parameter.getName();
                JsonNode value = arguments.get(paramName);
                values[i] = (value == null || value.isNull())
                        ? defaultValue(parameter.getType())
                        : objectMapper.convertValue(value, parameter.getType());
            }

            if (!entry.method.canAccess(entry.instance)) {
                entry.method.setAccessible(true);
            }
            Object result = entry.method.invoke(entry.instance, values);
            return serializeResult(result);
        } catch (Exception e) {
            if (e instanceof SynapseException synapseException) throw synapseException;
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new SynapseException("Tool '" + name + "' invocation failed: " + cause.getMessage(), e,
                    SynapseException.ExceptionType.TOOL_ERROR);
        }
    }

    List<String> toolNames() {
        List<String> names = new ArrayList<>(entries.size());
        for (ToolEntry entry : entries) {
            names.add(entry.name);
        }
        return names;
    }

    boolean isEmpty() {
        return entries.isEmpty();
    }

    private ToolEntry find(String name) {
        for (ToolEntry entry : entries) {
            if (entry.name.equals(name)) return entry;
        }
        return null;
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\u0000';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
    }

    private String serializeResult(Object result) {
        if (result == null) return "null";
        if (result instanceof String string) return string;
        if (result instanceof Number || result instanceof Boolean || result instanceof Character) {
            return String.valueOf(result);
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return String.valueOf(result);
        }
    }

    private record ToolEntry(String name, String description, Object instance, Method method) {
    }
}
