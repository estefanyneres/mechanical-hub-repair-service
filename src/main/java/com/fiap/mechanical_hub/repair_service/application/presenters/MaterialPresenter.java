package com.fiap.mechanical_hub.repair_service.application.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;

import java.util.List;

public interface MaterialPresenter {
    MaterialResponse toResponse(Material material);
    List<MaterialResponse> toResponseList(List<Material> materials);
}
