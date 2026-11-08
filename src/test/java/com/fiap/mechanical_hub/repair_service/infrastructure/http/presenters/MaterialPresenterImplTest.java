package com.fiap.mechanical_hub.repair_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.MaterialMock.defaultMaterial;
import static org.assertj.core.api.Assertions.assertThat;

class MaterialPresenterImplTest {

    private final MaterialPresenterImpl presenter = new MaterialPresenterImpl();

    @Test
    void toResponse_ShouldMapAllFields() {
        Material material = defaultMaterial();

        MaterialResponse response = presenter.toResponse(material);

        assertThat(response.id()).isEqualTo(material.getId());
        assertThat(response.name()).isEqualTo(material.getName());
        assertThat(response.description()).isEqualTo(material.getDescription());
        assertThat(response.unitPrice()).isEqualTo(material.getUnitPrice());
        assertThat(response.minStockQuantity()).isEqualTo(material.getMinStockQuantity());
        assertThat(response.createdAt()).isEqualTo(material.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(material.getUpdatedAt());
    }

    @Test
    void toResponseList_ShouldMapEveryItem() {
        List<MaterialResponse> responses = presenter.toResponseList(List.of(defaultMaterial(), defaultMaterial()));

        assertThat(responses).hasSize(2);
    }
}
