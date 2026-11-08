package com.fiap.mechanical_hub.repair_service.infrastructure.database.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTaskModelTest {

    @Test
    void shouldRoundTripAllFields() {
        OrderTaskModel model = new OrderTaskModel();
        OrderTaskMaterialItem item = new OrderTaskMaterialItem();
        item.setMaterialId("m1");
        item.setMaterialName("Óleo");
        item.setQuantity(4);

        model.setPk("OS#os1");
        model.setSk("ORDERTASK#t1");
        model.setEntityType(OrderTaskModel.ENTITY_TYPE);
        model.setId("t1");
        model.setServiceOrderId("os1");
        model.setServiceId("s1");
        model.setServiceName("Troca de óleo");
        model.setStatus("PENDENTE");
        model.setStartedAt(null);
        model.setFinishedAt(null);
        model.setMaterials(List.of(item));

        assertThat(model.getPk()).isEqualTo("OS#os1");
        assertThat(model.getSk()).isEqualTo("ORDERTASK#t1");
        assertThat(model.getEntityType()).isEqualTo("ORDERTASK");
        assertThat(model.getId()).isEqualTo("t1");
        assertThat(model.getServiceOrderId()).isEqualTo("os1");
        assertThat(model.getServiceId()).isEqualTo("s1");
        assertThat(model.getServiceName()).isEqualTo("Troca de óleo");
        assertThat(model.getStatus()).isEqualTo("PENDENTE");
        assertThat(model.getStartedAt()).isNull();
        assertThat(model.getFinishedAt()).isNull();
        assertThat(model.getMaterials()).containsExactly(item);
    }

    @Test
    void shouldDefaultMaterialsToEmptyList() {
        OrderTaskModel model = new OrderTaskModel();

        assertThat(model.getMaterials()).isEmpty();
    }
}
