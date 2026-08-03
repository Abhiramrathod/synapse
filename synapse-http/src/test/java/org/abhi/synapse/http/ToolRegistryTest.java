package org.abhi.synapse.http;

import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.ToolDefinition;
import org.abhi.synapse.core.tool.SynapseTool;
import org.abhi.synapse.core.tool.ToolParam;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SynapseTool(description = "Weather helpers")
    public static class WeatherTools {
        @SynapseTool(description = "Gets the weather for a city")
        public String getWeather(@ToolParam("The city") String city) {
            return "Sunny in " + city;
        }
    }

    public static class Calculator {
        @SynapseTool(description = "Adds two integers")
        public int add(@ToolParam("first operand") int a, @ToolParam("second operand") int b) {
            return a + b;
        }

        @SynapseTool(description = "Multiply")
        public long multiply(long a, long b) {
            return a * b;
        }

        public String notATool() {
            return "ignored";
        }
    }

    public enum Currency {
        USD, EUR
    }

    public static class Conversions {
        @SynapseTool(description = "Rates a currency")
        public String rate(@ToolParam("The currency") Currency currency) {
            return "rate:" + currency;
        }
    }

    @SynapseTool(description = "Class-level tools")
    public static class ClassLevelTools {
        public String greet(@ToolParam("Who") String who) {
            return "Hello " + who;
        }
    }

    @Test
    void exposesAnnotatedMethodsOnly() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new Calculator()));
        assertThat(registry.toolNames()).containsExactlyInAnyOrder("add", "multiply");
    }

    @Test
    void buildsToolDefinitionsWithSchema() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new Calculator()));
        List<ToolDefinition> definitions = registry.definitions();
        assertThat(definitions).hasSize(2);

        ToolDefinition add = definitions.stream()
                .filter(d -> d.getFunction().getName().equals("add"))
                .findFirst().orElseThrow();
        assertThat(add.getFunction().getDescription()).isEqualTo("Adds two integers");
        assertThat(add.getFunction().getParameters()).contains("\"type\":\"object\"");
        assertThat(add.getFunction().getParameters()).contains("\"a\"");
        assertThat(add.getFunction().getParameters()).contains("\"b\"");
        assertThat(add.getFunction().getParameters()).contains("\"required\"");
    }

    @Test
    void invokesMethodWithBoundArguments() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new Calculator()));
        assertThat(registry.invoke("add", "{\"a\":2,\"b\":3}")).isEqualTo("5");
        assertThat(registry.invoke("multiply", "{\"a\":4,\"b\":5}")).isEqualTo("20");
    }

    @Test
    void invokesWithStringArguments() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new WeatherTools()));
        assertThat(registry.invoke("getWeather", "{\"city\":\"Paris\"}")).isEqualTo("Sunny in Paris");
    }

    @Test
    void invokesWithMissingOptionalArgs() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new Calculator()));
        assertThat(registry.invoke("add", "{\"a\":2}")).isEqualTo("2");
    }

    @Test
    void bindsEnumArguments() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new Conversions()));
        assertThat(registry.invoke("rate", "{\"currency\":\"EUR\"}")).isEqualTo("rate:EUR");
    }

    @Test
    void unknownToolThrowsToolError() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new Calculator()));
        assertThatThrownBy(() -> registry.invoke("nope", "{}"))
                .isInstanceOf(SynapseException.class)
                .satisfies(e -> assertThat(((SynapseException) e).getType())
                        .isEqualTo(SynapseException.ExceptionType.TOOL_ERROR));
    }

    @Test
    void classLevelAnnotationExposesMethods() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of(new ClassLevelTools()));
        assertThat(registry.toolNames()).contains("greet");
        assertThat(registry.invoke("greet", "{\"who\":\"Ada\"}")).isEqualTo("Hello Ada");
    }

    @Test
    void emptyInstancesProducesEmptyRegistry() {
        ToolRegistry registry = new ToolRegistry(objectMapper, List.of());
        assertThat(registry.isEmpty()).isTrue();
        assertThat(registry.definitions()).isEmpty();
    }
}
