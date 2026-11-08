package com.fiap.mechanical_hub.repair_service.application.gateways;

import java.util.UUID;

public record MaterialQuantity(UUID materialId, int quantity) {
}
