package com.fiap.mechanical_hub.repair_service.domain.entities;

import java.util.UUID;

/**
 * Quanto de um material um servico do catalogo tipicamente usa. Substitui
 * o antigo ServiceMaterial do monolito -- aqui nao tem identidade/gateway
 * propria, e so um valor embutido dentro do item SERVICE# no DynamoDB
 * (ver ADR-003 / desenho da TASK-021).
 */
public record MaterialRequirement(UUID materialId, int quantity) {
}
