package org.abhi.synapse.core;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateTest {

    @Test
    void substitutesKnownVariables() {
        assertThat(PromptTemplate.render("Hello {name}, you asked about {topic}.",
                Map.of("name", "Alice", "topic", "Java")))
                .isEqualTo("Hello Alice, you asked about Java.");
    }

    @Test
    void leavesUnknownVariablesIntact() {
        assertThat(PromptTemplate.render("Hello {name} from {city}.",
                Map.of("name", "Alice")))
                .isEqualTo("Hello Alice from {city}.");
    }

    @Test
    void returnsTemplateUnchangedWhenNoVariables() {
        assertThat(PromptTemplate.render("Plain text", Map.of("a", "b"))).isEqualTo("Plain text");
        assertThat(PromptTemplate.render("Plain text", null)).isEqualTo("Plain text");
        assertThat(PromptTemplate.render("Plain text", Map.of())).isEqualTo("Plain text");
    }

    @Test
    void handlesNullTemplate() {
        assertThat(PromptTemplate.render(null, Map.of("a", "b"))).isNull();
    }

    @Test
    void injectsSpecialCharactersLiterally() {
        assertThat(PromptTemplate.render("cost {amount}", Map.of("amount", "$100 \\ per unit")))
                .isEqualTo("cost $100 \\ per unit");
    }

    @Test
    void convertsNonStringValuesToString() {
        assertThat(PromptTemplate.render("count {n} active={flag}", Map.of("n", 5, "flag", true)))
                .isEqualTo("count 5 active=true");
    }

    @Test
    void doesNotEvaluateExpressions() {
        assertThat(PromptTemplate.render("1+1={1+1}", Map.of("1+1", "2")))
                .isEqualTo("1+1={1+1}");
    }

    @Test
    void emptyValueSubstitution() {
        assertThat(PromptTemplate.render("Hello {name}", Map.of("name", "")))
                .isEqualTo("Hello ");
    }
}
