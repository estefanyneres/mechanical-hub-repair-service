package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.material.UpsertMaterialRequest;
import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.presenters.MaterialPresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.NotFoundException;

import java.util.List;
import java.util.UUID;

public class MaterialUseCase {

    public static final String MATERIAL_NOT_FOUND_MESSAGE = "Material não encontrado para o id: ";
    private final MaterialGateway materialGateway;
    private final StockServiceGateway stockServiceGateway;
    private final MaterialPresenter materialPresenter;

    public MaterialUseCase(MaterialGateway materialGateway, StockServiceGateway stockServiceGateway,
                           MaterialPresenter materialPresenter) {
        this.materialGateway = materialGateway;
        this.stockServiceGateway = stockServiceGateway;
        this.materialPresenter = materialPresenter;
    }

    public MaterialResponse create(UpsertMaterialRequest createRequest) {
        Material material = Material.create(
                createRequest.name(),
                createRequest.description(),
                createRequest.unitPrice(),
                createRequest.minStockQuantity()
        );

        Material savedMaterial = materialGateway.save(material);
        stockServiceGateway.initializeForMaterial(savedMaterial.getId());
        return materialPresenter.toResponse(savedMaterial);
    }

    public Material findById(UUID id) {
        return materialGateway.findById(id)
                .orElseThrow(() -> new NotFoundException(MATERIAL_NOT_FOUND_MESSAGE + id));
    }

    public MaterialResponse findMaterialById(UUID id) {
        return materialPresenter.toResponse(findById(id));
    }

    public List<MaterialResponse> findAll() {
        return materialPresenter.toResponseList(materialGateway.findAll());
    }

    public MaterialResponse update(UUID id, UpsertMaterialRequest updateRequest) {
        Material existingMaterial = materialGateway.findById(id)
                .orElseThrow(() -> new NotFoundException(MATERIAL_NOT_FOUND_MESSAGE + id));

        existingMaterial.update(
                updateRequest.name(),
                updateRequest.description(),
                updateRequest.unitPrice(),
                updateRequest.minStockQuantity());

        Material saved = materialGateway.save(existingMaterial);
        return materialPresenter.toResponse(saved);
    }

    public void delete(UUID id) {
        if (materialGateway.findById(id).isEmpty()) {
            throw new NotFoundException("Material não encontrado com o id: " + id);
        }

        stockServiceGateway.deleteByMaterialId(id);
        materialGateway.deleteById(id);
    }
}
