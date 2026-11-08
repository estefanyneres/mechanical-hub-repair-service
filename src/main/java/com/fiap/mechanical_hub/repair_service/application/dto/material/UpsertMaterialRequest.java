package com.fiap.mechanical_hub.repair_service.application.dto.material;

import java.math.BigDecimal;

public record UpsertMaterialRequest(
        String name,
        String description,
        BigDecimal unitPrice,
        Integer minStockQuantity
) {}
