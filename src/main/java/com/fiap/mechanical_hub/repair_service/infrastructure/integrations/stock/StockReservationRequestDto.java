package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.stock;

import java.util.List;
import java.util.UUID;

/**
 * Formato de fio pro POST /stock/reservations[/consume] do stock-service
 * (TASK-029) -- espelha o ReserveMaterialsRequest do lado de la.
 */
record StockReservationRequestDto(UUID serviceOrderId, List<MaterialQuantityDto> items) {
}
