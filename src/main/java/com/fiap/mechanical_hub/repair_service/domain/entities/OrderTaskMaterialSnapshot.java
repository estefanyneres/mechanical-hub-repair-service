package com.fiap.mechanical_hub.repair_service.domain.entities;

import java.util.UUID;

/**
 * Material efetivamente consumido por uma tarefa de execucao especifica
 * (pode divergir do MaterialRequirement padrao do catalogo). Substitui o
 * antigo OrderTaskMaterial do monolito -- sem identidade/gateway propria,
 * embutido dentro do item ORDERTASK# no DynamoDB. Sem preco unitario: o
 * calculo de orcamento nao e mais responsabilidade deste servico
 * (billing-service, Fase 4).
 */
public record OrderTaskMaterialSnapshot(UUID materialId, String materialName, int quantity) {
}
