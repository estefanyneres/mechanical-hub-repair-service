package com.fiap.mechanical_hub.repair_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskMaterialResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskResponse;
import com.fiap.mechanical_hub.repair_service.application.presenters.OrderTaskPresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderTaskPresenterImpl implements OrderTaskPresenter {

    @Override
    public OrderTaskResponse toResponse(OrderTask orderTask) {
        List<OrderTaskMaterialResponse> materials = orderTask.getMaterials().stream()
                .map(m -> new OrderTaskMaterialResponse(m.materialId(), m.materialName(), m.quantity()))
                .toList();

        return new OrderTaskResponse(
                orderTask.getId(),
                orderTask.getServiceOrderId(),
                orderTask.getServiceData().getId(),
                orderTask.getServiceData().getName(),
                orderTask.getStatus().name(),
                orderTask.getStartedAt(),
                orderTask.getFinishedAt(),
                materials
        );
    }

    @Override
    public List<OrderTaskResponse> toResponseList(List<OrderTask> orderTasks) {
        return orderTasks.stream().map(this::toResponse).toList();
    }
}
