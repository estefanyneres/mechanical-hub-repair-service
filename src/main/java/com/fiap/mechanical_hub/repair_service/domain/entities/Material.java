package com.fiap.mechanical_hub.repair_service.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Material {
    private final UUID id;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private Integer minStockQuantity;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Material(
            UUID uuid,
            String name,
            String description,
            BigDecimal unitPrice,
            Integer minStockQuantity,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = uuid;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.minStockQuantity = minStockQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Material create(String name, String description, BigDecimal unitPrice,
                                  Integer minStockQuantity) {
        return new Material(
                UUID.randomUUID(),
                name,
                description,
                unitPrice,
                minStockQuantity,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public void update(String name, String description, BigDecimal unitPrice, Integer minStockQuantity) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.minStockQuantity = minStockQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getMinStockQuantity() { return minStockQuantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
