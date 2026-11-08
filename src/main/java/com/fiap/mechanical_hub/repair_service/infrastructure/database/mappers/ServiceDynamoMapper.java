package com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.repair_service.domain.entities.MaterialRequirement;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.MaterialRequirementItem;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.ServiceModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ServiceDynamoMapper {

    private ServiceDynamoMapper() {}

    public static ServiceModel toModel(ServiceData serviceData) {
        ServiceModel model = new ServiceModel();
        model.setPk("SERVICE#" + serviceData.getId());
        model.setSk(ServiceModel.SK_VALUE);
        model.setEntityType(ServiceModel.ENTITY_TYPE);
        model.setId(serviceData.getId().toString());
        model.setName(serviceData.getName());
        model.setDescription(serviceData.getDescription());
        model.setLaborCost(serviceData.getLaborCost());
        model.setTotalPrice(serviceData.getTotalPrice());
        model.setActive(serviceData.isActive());
        model.setCreatedAt(serviceData.getCreatedAt() != null ? serviceData.getCreatedAt().toString() : null);
        model.setUpdatedAt(serviceData.getUpdatedAt() != null ? serviceData.getUpdatedAt().toString() : null);
        model.setDefaultMaterials(serviceData.getDefaultMaterials().stream().map(requirement -> {
            MaterialRequirementItem item = new MaterialRequirementItem();
            item.setMaterialId(requirement.materialId().toString());
            item.setQuantity(requirement.quantity());
            return item;
        }).toList());
        return model;
    }

    public static ServiceData toDomain(ServiceModel model) {
        List<MaterialRequirement> defaultMaterials = model.getDefaultMaterials().stream()
                .map(item -> new MaterialRequirement(UUID.fromString(item.getMaterialId()), item.getQuantity()))
                .toList();

        return new ServiceData(
                UUID.fromString(model.getId()),
                model.getName(),
                model.getDescription(),
                model.getLaborCost(),
                model.getTotalPrice(),
                Boolean.TRUE.equals(model.getActive()),
                model.getCreatedAt() != null ? LocalDateTime.parse(model.getCreatedAt()) : null,
                model.getUpdatedAt() != null ? LocalDateTime.parse(model.getUpdatedAt()) : null,
                defaultMaterials
        );
    }
}
