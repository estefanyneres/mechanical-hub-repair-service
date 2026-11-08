package com.fiap.mechanical_hub.repair_service.infrastructure.database.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialModelTest {

    @Test
    void shouldRoundTripAllFields() {
        MaterialModel model = new MaterialModel();

        model.setPk("MATERIAL#m1");
        model.setSk(MaterialModel.SK_VALUE);
        model.setEntityType(MaterialModel.ENTITY_TYPE);
        model.setId("m1");
        model.setName("Óleo 5W30");
        model.setDescription("desc");
        model.setUnitPrice(BigDecimal.valueOf(35));
        model.setMinStockQuantity(10);
        model.setCreatedAt("2026-01-01T00:00:00");
        model.setUpdatedAt("2026-01-01T00:00:00");

        assertThat(model.getPk()).isEqualTo("MATERIAL#m1");
        assertThat(model.getSk()).isEqualTo("METADATA");
        assertThat(model.getEntityType()).isEqualTo("MATERIAL");
        assertThat(model.getId()).isEqualTo("m1");
        assertThat(model.getName()).isEqualTo("Óleo 5W30");
        assertThat(model.getDescription()).isEqualTo("desc");
        assertThat(model.getUnitPrice()).isEqualTo(BigDecimal.valueOf(35));
        assertThat(model.getMinStockQuantity()).isEqualTo(10);
        assertThat(model.getCreatedAt()).isEqualTo("2026-01-01T00:00:00");
        assertThat(model.getUpdatedAt()).isEqualTo("2026-01-01T00:00:00");
    }
}
