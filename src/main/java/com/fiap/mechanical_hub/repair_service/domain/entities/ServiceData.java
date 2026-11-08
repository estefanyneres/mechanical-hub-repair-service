package com.fiap.mechanical_hub.repair_service.domain.entities;

import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceData {

    private final UUID id;
    private String name;
    private String description;
    private BigDecimal laborCost;
    private BigDecimal totalPrice;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MaterialRequirement> defaultMaterials;

    public ServiceData() {
        this.id = null;
        this.createdAt = null;
        this.defaultMaterials = new ArrayList<>();
    }

    public ServiceData(UUID id, String name, String description, BigDecimal laborCost,
                       BigDecimal totalPrice, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
                       List<MaterialRequirement> defaultMaterials) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.laborCost = laborCost;
        this.totalPrice = totalPrice;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.defaultMaterials = defaultMaterials != null ? defaultMaterials : new ArrayList<>();
    }

    public static ServiceData create(String name, String description, BigDecimal laborCost) {
        validateInputs(name, laborCost);
        return new ServiceData(
                UUID.randomUUID(), name, description, laborCost,
                laborCost, true, LocalDateTime.now(), LocalDateTime.now(), new ArrayList<>()
        );
    }

    public void update(String name, String description, BigDecimal laborCost) {
        validateInputs(name, laborCost);
        this.name = name;
        this.description = description;
        this.laborCost = laborCost;
        this.totalPrice = laborCost;
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateInputs(String name, BigDecimal laborCost) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException("Nome do serviço é obrigatório");
        }
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Custo de mão de obra deve ser positivo");
        }
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDefaultMaterials(List<MaterialRequirement> defaultMaterials) {
        this.defaultMaterials = defaultMaterials != null ? defaultMaterials : new ArrayList<>();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getLaborCost() { return laborCost; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<MaterialRequirement> getDefaultMaterials() { return defaultMaterials; }
}
