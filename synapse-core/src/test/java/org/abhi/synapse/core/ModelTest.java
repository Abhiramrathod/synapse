package org.abhi.synapse.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    @Test
    void builderCreatesModel() {
        Model model = Model.builder()
                .id("gpt-4")
                .object("model")
                .created(1687100000L)
                .ownedBy("openai")
                .build();

        assertThat(model.getId()).isEqualTo("gpt-4");
        assertThat(model.getObject()).isEqualTo("model");
        assertThat(model.getCreated()).isEqualTo(1687100000L);
        assertThat(model.getOwnedBy()).isEqualTo("openai");
    }

    @Test
    void defaultConstructorWorks() {
        Model model = new Model();
        assertThat(model.getId()).isNull();
    }

    @Test
    void settersWork() {
        Model model = new Model();
        model.setId("gpt-4");
        model.setObject("model");
        model.setCreated(123L);
        model.setOwnedBy("openai");

        assertThat(model.getId()).isEqualTo("gpt-4");
        assertThat(model.getObject()).isEqualTo("model");
        assertThat(model.getCreated()).isEqualTo(123L);
        assertThat(model.getOwnedBy()).isEqualTo("openai");
    }
}
