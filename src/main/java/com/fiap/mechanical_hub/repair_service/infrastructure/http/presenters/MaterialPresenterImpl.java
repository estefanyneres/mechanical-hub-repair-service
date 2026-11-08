package com.fiap.mechanical_hub.repair_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.application.presenters.MaterialPresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MaterialPresenterImpl implements MaterialPresenter {

    @Override
    public MaterialResponse toResponse(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getName(),
                material.getDescription(),
                material.getUnitPrice(),
                material.getMinStockQuantity(),
                material.getCreatedAt(),
                material.getUpdatedAt()
        );
    }

    @Override
    public List<MaterialResponse> toResponseList(List<Material> materials) {
        return materials.stream().map(this::toResponse).toList();
    }
}
