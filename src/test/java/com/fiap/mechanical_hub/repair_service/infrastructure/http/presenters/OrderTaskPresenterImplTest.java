package com.fiap.mechanical_hub.repair_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskResponse;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTaskMaterialSnapshot;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.ServiceMock.defaultService;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTaskPresenterImplTest {

    private final OrderTaskPresenterImpl presenter = new OrderTaskPresenterImpl();

    @Test
    void toResponse_ShouldMapAllFieldsIncludingMaterials() {
        ServiceData service = defaultService();
        UUID serviceOrderId = UUID.randomUUID();
        OrderTaskMaterialSnapshot snapshot = new OrderTaskMaterialSnapshot(UUID.randomUUID(), "Óleo", 4);
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of(snapshot));

        OrderTaskResponse response = presenter.toResponse(task);

        assertThat(response.id()).isEqualTo(task.getId());
        assertThat(response.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(response.serviceId()).isEqualTo(service.getId());
        assertThat(response.serviceName()).isEqualTo(service.getName());
        assertThat(response.status()).isEqualTo(task.getStatus().name());
        assertThat(response.startedAt()).isNull();
        assertThat(response.finishedAt()).isNull();
        assertThat(response.materials()).hasSize(1);
        assertThat(response.materials().get(0).materialId()).isEqualTo(snapshot.materialId());
        assertThat(response.materials().get(0).materialName()).isEqualTo("Óleo");
        assertThat(response.materials().get(0).quantity()).isEqualTo(4);
    }

    @Test
    void toResponseList_ShouldMapEveryItem() {
        ServiceData service = defaultService();
        OrderTask task1 = OrderTask.create(UUID.randomUUID(), service, List.of());
        OrderTask task2 = OrderTask.create(UUID.randomUUID(), service, List.of());

        List<OrderTaskResponse> responses = presenter.toResponseList(List.of(task1, task2));

        assertThat(responses).hasSize(2);
    }
}
