package com.fiap.mechanical_hub.repair_service.infrastructure.database.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialRequirementItemTest {

    @Test
    void shouldRoundTripAllFields() {
        MaterialRequirementItem item = new MaterialRequirementItem();

        item.setMaterialId("m1");
        item.setQuantity(2);

        assertThat(item.getMaterialId()).isEqualTo("m1");
        assertThat(item.getQuantity()).isEqualTo(2);
    }
}
