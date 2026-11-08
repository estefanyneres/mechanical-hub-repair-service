package com.fiap.mechanical_hub.repair_service.application.dto.ordertask;

import java.util.UUID;

public record MaterialQuantityRequest(UUID materialId, int quantity) {
}
