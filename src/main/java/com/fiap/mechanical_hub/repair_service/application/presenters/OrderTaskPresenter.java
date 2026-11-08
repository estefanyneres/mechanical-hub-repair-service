package com.fiap.mechanical_hub.repair_service.application.presenters;

import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskResponse;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;

import java.util.List;

public interface OrderTaskPresenter {
    OrderTaskResponse toResponse(OrderTask orderTask);
    List<OrderTaskResponse> toResponseList(List<OrderTask> orderTasks);
}
