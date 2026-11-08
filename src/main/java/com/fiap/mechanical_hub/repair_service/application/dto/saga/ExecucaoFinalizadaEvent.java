package com.fiap.mechanical_hub.repair_service.application.dto.saga;

import java.util.UUID;

public record ExecucaoFinalizadaEvent(UUID serviceOrderId) {
}
