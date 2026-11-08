package com.fiap.mechanical_hub.repair_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.ServiceMock.defaultService;
import static org.assertj.core.api.Assertions.assertThat;

class ServicePresenterImplTest {

    private final ServicePresenterImpl presenter = new ServicePresenterImpl();

    @Test
    void toResponse_ShouldMapAllFields() {
        ServiceData service = defaultService();

        ServiceResponse response = presenter.toResponse(service);

        assertThat(response.id()).isEqualTo(service.getId());
        assertThat(response.name()).isEqualTo(service.getName());
        assertThat(response.description()).isEqualTo(service.getDescription());
        assertThat(response.laborCost()).isEqualTo(service.getLaborCost());
        assertThat(response.totalPrice()).isEqualTo(service.getTotalPrice());
        assertThat(response.active()).isEqualTo(service.isActive());
        assertThat(response.createdAt()).isEqualTo(service.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(service.getUpdatedAt());
    }

    @Test
    void toResponseList_ShouldMapEveryItem() {
        List<ServiceResponse> responses = presenter.toResponseList(List.of(defaultService(), defaultService()));

        assertThat(responses).hasSize(2);
    }
}
