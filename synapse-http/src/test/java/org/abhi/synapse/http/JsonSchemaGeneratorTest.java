package org.abhi.synapse.http;

import org.abhi.synapse.core.tool.ToolParam;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaGeneratorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode schemaOf(String methodName) throws Exception {
        Method method = Samples.class.getMethod(methodName, String.class);
        return JsonSchemaGenerator.generateSchema(method.getParameters(), objectMapper);
    }

    static class Samples {
        public void scalars(String city, int count, double ratio, boolean flag) {
        }

        public void enumParam(Size size) {
        }

        public void listParam(List<String> names) {
        }

        public void pojoParam(Address address) {
        }

        public void annotated(@ToolParam(description = "The city name") String city) {
        }

        public void noArgs() {
        }
    }

    enum Size {
        SMALL, LARGE
    }

    static class Address {
        private String street;
        private int zip;
    }

    @Test
    void generatesScalarTypes() throws Exception {
        Method m = Samples.class.getMethod("scalars", String.class, int.class, double.class, boolean.class);
        JsonNode schema = JsonSchemaGenerator.generateSchema(m.getParameters(), objectMapper);

        assertThat(schema.path("type").asText()).isEqualTo("object");
        assertThat(schema.path("properties").path("city").path("type").asText()).isEqualTo("string");
        assertThat(schema.path("properties").path("count").path("type").asText()).isEqualTo("integer");
        assertThat(schema.path("properties").path("ratio").path("type").asText()).isEqualTo("number");
        assertThat(schema.path("properties").path("flag").path("type").asText()).isEqualTo("boolean");
        assertThat(schema.path("required")).hasSize(4);
    }

    @Test
    void generatesEnumSchema() throws Exception {
        Method m = Samples.class.getMethod("enumParam", Size.class);
        JsonNode schema = JsonSchemaGenerator.generateSchema(m.getParameters(), objectMapper);
        JsonNode enumNode = schema.path("properties").path("size").path("enum");
        assertThat(schema.path("properties").path("size").path("type").asText()).isEqualTo("string");
        assertThat(enumNode).hasSize(2);
        assertThat(enumNode.get(0).asText()).isEqualTo("SMALL");
    }

    @Test
    void generatesArrayOfStrings() throws Exception {
        Method m = Samples.class.getMethod("listParam", List.class);
        JsonNode schema = JsonSchemaGenerator.generateSchema(m.getParameters(), objectMapper);
        JsonNode items = schema.path("properties").path("names").path("items");
        assertThat(schema.path("properties").path("names").path("type").asText()).isEqualTo("array");
        assertThat(items.path("type").asText()).isEqualTo("string");
    }

    @Test
    void generatesNestedPojoSchema() throws Exception {
        Method m = Samples.class.getMethod("pojoParam", Address.class);
        JsonNode schema = JsonSchemaGenerator.generateSchema(m.getParameters(), objectMapper);
        JsonNode address = schema.path("properties").path("address");
        assertThat(address.path("type").asText()).isEqualTo("object");
        assertThat(address.path("properties").path("street").path("type").asText()).isEqualTo("string");
        assertThat(address.path("properties").path("zip").path("type").asText()).isEqualTo("integer");
    }

    @Test
    void includesParamDescription() throws Exception {
        JsonNode schema = schemaOf("annotated");
        assertThat(schema.path("properties").path("city").path("description").asText())
                .isEqualTo("The city name");
    }

    @Test
    void noArgsYieldsEmptySchema() throws Exception {
        Method m = Samples.class.getMethod("noArgs");
        JsonNode schema = JsonSchemaGenerator.generateSchema(m.getParameters(), objectMapper);
        assertThat(schema.path("type").asText()).isEqualTo("object");
        assertThat(schema.path("properties")).isEmpty();
        assertThat(schema.path("required")).isEmpty();
    }
}
