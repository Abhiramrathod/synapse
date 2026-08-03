package org.abhi.synapse.core.model;

/**
 * Describes the desired response format of an LLM call.
 *
 * <p>Supports plain text, {@code json_object}, and provider-native
 * {@code json_schema} structured output. Structured output carries the JSON
 * Schema (as a JSON string) that the model's reply must conform to.</p>
 */
public class ResponseFormat {
    private String type;
    private String name;
    private String schemaJson;

    public ResponseFormat() {
    }

    public ResponseFormat(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemaJson() {
        return schemaJson;
    }

    public void setSchemaJson(String schemaJson) {
        this.schemaJson = schemaJson;
    }

    public static ResponseFormat json() {
        return new ResponseFormat("json_object");
    }

    public static ResponseFormat text() {
        return new ResponseFormat("text");
    }

    /**
     * Requests provider-native JSON Schema structured output.
     *
     * @param name       the schema name reported to the provider
     * @param schemaJson the JSON Schema document (as a JSON string) the output must satisfy
     * @return a {@code json_schema} response format
     */
    public static ResponseFormat jsonSchema(String name, String schemaJson) {
        ResponseFormat format = new ResponseFormat("json_schema");
        format.setName(name);
        format.setSchemaJson(schemaJson);
        return format;
    }
}
