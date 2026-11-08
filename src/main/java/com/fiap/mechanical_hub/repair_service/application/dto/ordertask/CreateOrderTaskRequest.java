package com.fiap.mechanical_hub.repair_service.application.dto.ordertask;

import java.util.List;
import java.util.UUID;

/**
 * materials e opcional: se omitido, usa o defaultMaterials do servico do
 * catalogo (ServiceData); se informado, substitui pela lista explicita do
 * chamador.
 */
public record CreateOrderTaskRequest(UUID serviceId, List<MaterialQuantityRequest> materials) {
}
