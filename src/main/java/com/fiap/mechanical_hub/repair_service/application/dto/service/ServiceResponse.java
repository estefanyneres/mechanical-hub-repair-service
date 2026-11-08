package com.fiap.mechanical_hub.repair_service.application.dto.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String name,
        String description,
        BigDecimal laborCost,
        BigDecimal totalPrice,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
