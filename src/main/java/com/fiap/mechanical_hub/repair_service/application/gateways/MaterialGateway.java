package com.fiap.mechanical_hub.repair_service.application.gateways;

import com.fiap.mechanical_hub.repair_service.domain.entities.Material;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaterialGateway {
    Material save(Material material);
    Optional<Material> findById(UUID id);
    List<Material> findAll();
    void deleteById(UUID id);
}
