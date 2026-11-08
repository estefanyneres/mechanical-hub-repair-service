package com.fiap.mechanical_hub.repair_service.infrastructure.database.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTaskMaterialItemTest {

    @Test
    void shouldRoundTripAllFields() {
        OrderTaskMaterialItem item = new OrderTaskMaterialItem();

        item.setMaterialId("m1");
        item.setMaterialName("Óleo 5W30");
        item.setQuantity(4);

        assertThat(item.getMaterialId()).isEqualTo("m1");
        assertThat(item.getMaterialName()).isEqualTo("Óleo 5W30");
        assertThat(item.getQuantity()).isEqualTo(4);
    }
}
