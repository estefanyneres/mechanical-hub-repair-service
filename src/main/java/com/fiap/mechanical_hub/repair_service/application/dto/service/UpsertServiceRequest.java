package com.fiap.mechanical_hub.repair_service.application.dto.service;

import java.math.BigDecimal;

public record UpsertServiceRequest(
        String name,
        String description,
        BigDecimal laborCost
) {}
