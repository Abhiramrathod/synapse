package org.abhi.synapse.http;

import org.abhi.synapse.core.tool.ToolParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Derives a JSON Schema (OpenAPI subset) object for a tool method's parameters.
 *
 * <p>Supported parameter types: {@code String}, {@code boolean}/{@code Boolean},
 * integral and floating point types, enums, {@code List}/{@code Set} of scalar
 * or nested types, {@code Map}, and plain POJOs / records (recursively
 * introspected via their declared fields).</p>
 */
final class JsonSchemaGenerator {

    private JsonSchemaGenerator() {
    }

    static JsonNode generateSchema(Parameter[] parameters, ObjectMapper objectMapper) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "object");
        ObjectNode properties = root.putObject("properties");
        ArrayNode required = root.putArray("required");

        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            ObjectNode property = typeFor(parameter.getParameterizedType(), parameter.getType(),
                    objectMapper, new HashSet<>());
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);
            String description = toolParam == null ? "" : toolParam.description().isEmpty()
                    ? toolParam.value()
                    : toolParam.description();
            if (!description.isEmpty()) {
                property.put("description", description);
            }
            properties.set(name, property);
            if (toolParam == null || toolParam.required()) {
                required.add(name);
            }
        }
        return root;
    }

    /**
     * Generates an object JSON Schema for structured output of the given type.
     *
     * @throws IllegalArgumentException when the type is not a POJO/record
     *                                  (i.e. a scalar type that cannot form an object schema)
     */
    static JsonNode generateObjectSchema(Class<?> type, ObjectMapper objectMapper) {
        if (!isObjectType(type)) {
            throw new IllegalArgumentException(
                    "Structured output requires a POJO or record type, got: " + type.getName());
        }
        return typeFor(Object.class, type, objectMapper, new HashSet<>());
    }

    private static boolean isObjectType(Class<?> type) {
        if (type == String.class || type == Boolean.class || type == Character.class
                || type.isPrimitive() || Number.class.isAssignableFrom(type)
                || type == Object.class || type.isEnum()) {
            return false;
        }
        return true;
    }

    private static ObjectNode typeFor(Type genericType, Class<?> raw, ObjectMapper objectMapper, Set<Class<?>> seen) {
        ObjectNode node = objectMapper.createObjectNode();
        if (raw == String.class || raw == Character.class || raw == char.class) {
            node.put("type", "string");
        } else if (raw == boolean.class || raw == Boolean.class) {
            node.put("type", "boolean");
        } else if (raw == byte.class || raw == Byte.class || raw == short.class || raw == Short.class
                || raw == int.class || raw == Integer.class || raw == long.class || raw == Long.class) {
            node.put("type", "integer");
        } else if (raw == float.class || raw == Float.class || raw == double.class || raw == Double.class) {
            node.put("type", "number");
        } else if (raw.isEnum()) {
            node.put("type", "string");
            ArrayNode values = node.putArray("enum");
            for (Object constant : raw.getEnumConstants()) {
                values.add(((Enum<?>) constant).name());
            }
        } else if (List.class.isAssignableFrom(raw) || Set.class.isAssignableFrom(raw)
                || Collection.class.isAssignableFrom(raw)) {
            node.put("type", "array");
            Type itemType = itemType(genericType);
            Class<?> itemRaw = itemRaw(itemType);
            node.set("items", typeFor(itemType, itemRaw, objectMapper, seen));
        } else if (Map.class.isAssignableFrom(raw)) {
            node.put("type", "object");
            node.put("additionalProperties", true);
        } else {
            ObjectNode object = objectMapper.createObjectNode();
            object.put("type", "object");
            if (!seen.contains(raw)) {
                seen.add(raw);
                ObjectNode nestedProperties = object.putObject("properties");
                for (Field field : raw.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                        continue;
                    }
                    nestedProperties.set(field.getName(),
                            typeFor(field.getGenericType(), field.getType(), objectMapper, seen));
                }
                seen.remove(raw);
            }
            node.setAll(object);
        }
        return node;
    }

    private static Type itemType(Type genericType) {
        if (genericType instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (args.length > 0) return args[0];
        }
        return Object.class;
    }

    private static Class<?> itemRaw(Type itemType) {
        if (itemType instanceof Class<?> clazz) return clazz;
        if (itemType instanceof ParameterizedType parameterized
                && parameterized.getRawType() instanceof Class<?> clazz) return clazz;
        return Object.class;
    }
}
