package com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.MaterialModel;

import java.time.LocalDateTime;
import java.util.UUID;

public class MaterialDynamoMapper {

    private MaterialDynamoMapper() {}

    public static MaterialModel toModel(Material material) {
        MaterialModel model = new MaterialModel();
        model.setPk("MATERIAL#" + material.getId());
        model.setSk(MaterialModel.SK_VALUE);
        model.setEntityType(MaterialModel.ENTITY_TYPE);
        model.setId(material.getId().toString());
        model.setName(material.getName());
        model.setDescription(material.getDescription());
        model.setUnitPrice(material.getUnitPrice());
        model.setMinStockQuantity(material.getMinStockQuantity());
        model.setCreatedAt(material.getCreatedAt() != null ? material.getCreatedAt().toString() : null);
        model.setUpdatedAt(material.getUpdatedAt() != null ? material.getUpdatedAt().toString() : null);
        return model;
    }

    public static Material toDomain(MaterialModel model) {
        return new Material(
                UUID.fromString(model.getId()),
                model.getName(),
                model.getDescription(),
                model.getUnitPrice(),
                model.getMinStockQuantity(),
                model.getCreatedAt() != null ? LocalDateTime.parse(model.getCreatedAt()) : null,
                model.getUpdatedAt() != null ? LocalDateTime.parse(model.getUpdatedAt()) : null
        );
    }
}
