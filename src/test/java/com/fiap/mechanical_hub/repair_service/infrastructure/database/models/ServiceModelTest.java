package com.fiap.mechanical_hub.repair_service.infrastructure.database.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceModelTest {

    @Test
    void shouldRoundTripAllFields() {
        ServiceModel model = new ServiceModel();
        MaterialRequirementItem requirement = new MaterialRequirementItem();
        requirement.setMaterialId("m1");
        requirement.setQuantity(2);

        model.setPk("SERVICE#s1");
        model.setSk(ServiceModel.SK_VALUE);
        model.setEntityType(ServiceModel.ENTITY_TYPE);
        model.setId("s1");
        model.setName("Troca de óleo");
        model.setDescription("desc");
        model.setLaborCost(BigDecimal.TEN);
        model.setTotalPrice(BigDecimal.TEN);
        model.setActive(true);
        model.setCreatedAt("2026-01-01T00:00:00");
        model.setUpdatedAt("2026-01-01T00:00:00");
        model.setDefaultMaterials(List.of(requirement));

        assertThat(model.getPk()).isEqualTo("SERVICE#s1");
        assertThat(model.getSk()).isEqualTo("METADATA");
        assertThat(model.getEntityType()).isEqualTo("SERVICE");
        assertThat(model.getId()).isEqualTo("s1");
        assertThat(model.getName()).isEqualTo("Troca de óleo");
        assertThat(model.getDescription()).isEqualTo("desc");
        assertThat(model.getLaborCost()).isEqualTo(BigDecimal.TEN);
        assertThat(model.getTotalPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(model.getActive()).isTrue();
        assertThat(model.getCreatedAt()).isEqualTo("2026-01-01T00:00:00");
        assertThat(model.getUpdatedAt()).isEqualTo("2026-01-01T00:00:00");
        assertThat(model.getDefaultMaterials()).containsExactly(requirement);
    }

    @Test
    void shouldDefaultMaterialsToEmptyList() {
        ServiceModel model = new ServiceModel();

        assertThat(model.getDefaultMaterials()).isEmpty();
    }
}
