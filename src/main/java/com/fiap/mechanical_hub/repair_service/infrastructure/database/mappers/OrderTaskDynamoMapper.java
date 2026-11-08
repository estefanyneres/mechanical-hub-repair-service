package com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTaskMaterialSnapshot;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.domain.enums.TaskStatusEnum;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.OrderTaskMaterialItem;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.OrderTaskModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * toDomain so recria o ServiceData "denormalizado" (id + nome), nao o
 * catalogo completo -- se for preciso o servico inteiro, buscar via
 * ServiceGateway separadamente usando getServiceId().
 */
public class OrderTaskDynamoMapper {

    private OrderTaskDynamoMapper() {}

    public static OrderTaskModel toModel(OrderTask orderTask) {
        OrderTaskModel model = new OrderTaskModel();
        model.setPk("OS#" + orderTask.getServiceOrderId());
        model.setSk("ORDERTASK#" + orderTask.getId());
        model.setEntityType(OrderTaskModel.ENTITY_TYPE);
        model.setId(orderTask.getId().toString());
        model.setServiceOrderId(orderTask.getServiceOrderId().toString());
        model.setServiceId(orderTask.getServiceData().getId().toString());
        model.setServiceName(orderTask.getServiceData().getName());
        model.setStatus(orderTask.getStatus().name());
        model.setStartedAt(orderTask.getStartedAt() != null ? orderTask.getStartedAt().toString() : null);
        model.setFinishedAt(orderTask.getFinishedAt() != null ? orderTask.getFinishedAt().toString() : null);
        model.setMaterials(orderTask.getMaterials().stream().map(snapshot -> {
            OrderTaskMaterialItem item = new OrderTaskMaterialItem();
            item.setMaterialId(snapshot.materialId().toString());
            item.setMaterialName(snapshot.materialName());
            item.setQuantity(snapshot.quantity());
            return item;
        }).toList());
        return model;
    }

    public static OrderTask toDomain(OrderTaskModel model) {
        List<OrderTaskMaterialSnapshot> materials = model.getMaterials().stream()
                .map(item -> new OrderTaskMaterialSnapshot(
                        UUID.fromString(item.getMaterialId()), item.getMaterialName(), item.getQuantity()))
                .toList();

        // ServiceData "denormalizado": so id+nome, sem catalogo completo.
        ServiceData serviceStub = new ServiceData(
                UUID.fromString(model.getServiceId()), model.getServiceName(),
                null, null, null, true, null, null, List.of());

        return new OrderTask(
                UUID.fromString(model.getId()),
                UUID.fromString(model.getServiceOrderId()),
                serviceStub,
                materials,
                TaskStatusEnum.fromString(model.getStatus()),
                model.getStartedAt() != null ? LocalDateTime.parse(model.getStartedAt()) : null,
                model.getFinishedAt() != null ? LocalDateTime.parse(model.getFinishedAt()) : null
        );
    }
}
