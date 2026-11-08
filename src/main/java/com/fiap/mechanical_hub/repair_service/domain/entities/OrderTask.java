package com.fiap.mechanical_hub.repair_service.domain.entities;

import com.fiap.mechanical_hub.repair_service.domain.enums.TaskStatusEnum;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderTask {

    private UUID id;
    private UUID serviceOrderId;
    private ServiceData serviceData;
    private List<OrderTaskMaterialSnapshot> materials;
    private TaskStatusEnum status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public OrderTask() {}

    public OrderTask(UUID id, UUID serviceOrderId, ServiceData serviceData, List<OrderTaskMaterialSnapshot> materials,
                     TaskStatusEnum status, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.id = id;
        this.serviceOrderId = serviceOrderId;
        this.serviceData = serviceData;
        this.materials = materials != null ? materials : new ArrayList<>();
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static OrderTask create(UUID serviceOrderId, ServiceData serviceData, List<OrderTaskMaterialSnapshot> materials) {
        OrderTask task = new OrderTask();
        task.id = UUID.randomUUID();
        task.serviceOrderId = serviceOrderId;
        task.serviceData = serviceData;
        task.materials = materials != null ? materials : new ArrayList<>();
        task.status = TaskStatusEnum.PENDENTE;
        task.startedAt = null;
        task.finishedAt = null;

        return task;
    }

    public void start() {
        if (status != TaskStatusEnum.PENDENTE) {
            throw new BusinessRuleException("Transição de status não permitida");
        }

        this.status = TaskStatusEnum.INICIADO;
        this.startedAt = LocalDateTime.now();
    }

    public void finish() {
        if (status != TaskStatusEnum.INICIADO) {
            throw new BusinessRuleException("Transição de status não permitida");
        }

        this.status = TaskStatusEnum.FINALIZADO;
        this.finishedAt = LocalDateTime.now();
    }

    public boolean isFinished() {
        return status == TaskStatusEnum.FINALIZADO;
    }

    public UUID getId() { return id; }
    public UUID getServiceOrderId() { return serviceOrderId; }
    public ServiceData getServiceData() { return serviceData; }
    public List<OrderTaskMaterialSnapshot> getMaterials() { return materials; }
    public TaskStatusEnum getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
}
