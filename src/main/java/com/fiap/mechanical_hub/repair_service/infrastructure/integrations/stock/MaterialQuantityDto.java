package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import java.util.UUID;

record MaterialQuantityDto(UUID materialId, Integer quantity) {
}
