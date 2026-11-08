package com.fiap.mechanical_hub.repair_service.application.dto.ordertask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderTaskResponse(
        UUID id,
        UUID serviceOrderId,
        UUID serviceId,
        String serviceName,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        List<OrderTaskMaterialResponse> materials
) {}
