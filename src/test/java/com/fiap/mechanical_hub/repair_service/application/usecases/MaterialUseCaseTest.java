package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.material.UpsertMaterialRequest;
import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.presenters.MaterialPresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MaterialUseCase")
class MaterialUseCaseTest {

    @Mock
    private MaterialGateway materialGateway;

    @Mock
    private StockServiceGateway stockServiceGateway;

    @Mock
    private MaterialPresenter materialPresenter;

    @InjectMocks
    private MaterialUseCase materialUseCase;

    private UUID materialId;
    private Material material;
    private UpsertMaterialRequest upsertRequest;

    @BeforeEach
    void setUp() {
        materialId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        material = new Material(materialId, "Óleo de Motor", "Óleo sintético para motores",
                BigDecimal.valueOf(50.00), 10, now, now);

        upsertRequest = new UpsertMaterialRequest("Óleo de Motor", "Óleo sintético para motores",
                BigDecimal.valueOf(50.00), 10);
    }

    @Test
    @DisplayName("Deve criar material com sucesso")
    void shouldCreateMaterialSuccessfully() {
        Material createdMaterial = Material.create("Óleo de Motor", "Óleo sintético para motores",
                BigDecimal.valueOf(50.00), 10);
        MaterialResponse expectedResponse = new MaterialResponse(createdMaterial.getId(), "Óleo de Motor",
                "Óleo sintético para motores", BigDecimal.valueOf(50.00), 10, LocalDateTime.now(), LocalDateTime.now());
        when(materialGateway.save(any(Material.class))).thenReturn(createdMaterial);
        doNothing().when(stockServiceGateway).initializeForMaterial(any(UUID.class));
        when(materialPresenter.toResponse(createdMaterial)).thenReturn(expectedResponse);

        MaterialResponse result = materialUseCase.create(upsertRequest);

        assertNotNull(result);
        assertEquals("Óleo de Motor", result.name());
        verify(stockServiceGateway).initializeForMaterial(any(UUID.class));
    }

    @Test
    @DisplayName("Deve encontrar material por ID com sucesso")
    void shouldFindMaterialByIdSuccessfully() {
        MaterialResponse expectedResponse = new MaterialResponse(materialId, "Óleo de Motor",
                "Óleo sintético para motores", BigDecimal.valueOf(50.00), 10, LocalDateTime.now(), LocalDateTime.now());
        when(materialGateway.findById(materialId)).thenReturn(Optional.of(material));
        when(materialPresenter.toResponse(material)).thenReturn(expectedResponse);

        MaterialResponse result = materialUseCase.findMaterialById(materialId);

        assertEquals(materialId, result.id());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando material não encontrado por ID")
    void shouldThrowNotFoundWhenMaterialNotFoundById() {
        when(materialGateway.findById(materialId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> materialUseCase.findMaterialById(materialId));
    }

    @Test
    @DisplayName("Deve listar todos os materiais")
    void shouldFindAllMaterials() {
        List<Material> materials = List.of(material);
        MaterialResponse materialResponse = new MaterialResponse(materialId, "Óleo de Motor",
                "Óleo sintético para motores", BigDecimal.valueOf(50.00), 10, LocalDateTime.now(), LocalDateTime.now());
        when(materialGateway.findAll()).thenReturn(materials);
        when(materialPresenter.toResponseList(materials)).thenReturn(List.of(materialResponse));

        List<MaterialResponse> result = materialUseCase.findAll();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve atualizar material com sucesso")
    void shouldUpdateMaterialSuccessfully() {
        UpsertMaterialRequest updateRequest = new UpsertMaterialRequest("Óleo de Motor Premium",
                "Óleo sintético premium para motores", BigDecimal.valueOf(70.00), 15);

        Material updatedMaterial = new Material(materialId, "Óleo de Motor Premium",
                "Óleo sintético premium para motores", BigDecimal.valueOf(70.00), 15,
                material.getCreatedAt(), LocalDateTime.now());

        MaterialResponse expectedResponse = new MaterialResponse(materialId, "Óleo de Motor Premium",
                "Óleo sintético premium para motores", BigDecimal.valueOf(70.00), 15, LocalDateTime.now(), LocalDateTime.now());
        when(materialGateway.findById(materialId)).thenReturn(Optional.of(material));
        when(materialGateway.save(any(Material.class))).thenReturn(updatedMaterial);
        when(materialPresenter.toResponse(updatedMaterial)).thenReturn(expectedResponse);

        MaterialResponse result = materialUseCase.update(materialId, updateRequest);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao atualizar material não encontrado")
    void shouldThrowNotFoundWhenUpdatingNonExistentMaterial() {
        UpsertMaterialRequest updateRequest = new UpsertMaterialRequest("X", "Y", BigDecimal.TEN, 1);
        when(materialGateway.findById(materialId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> materialUseCase.update(materialId, updateRequest));
        verify(materialGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar material com sucesso")
    void shouldDeleteMaterialSuccessfully() {
        when(materialGateway.findById(materialId)).thenReturn(Optional.of(material));
        doNothing().when(stockServiceGateway).deleteByMaterialId(materialId);
        doNothing().when(materialGateway).deleteById(materialId);

        assertDoesNotThrow(() -> materialUseCase.delete(materialId));

        verify(stockServiceGateway).deleteByMaterialId(materialId);
        verify(materialGateway).deleteById(materialId);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao deletar material não encontrado")
    void shouldThrowNotFoundWhenDeletingNonExistentMaterial() {
        when(materialGateway.findById(materialId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> materialUseCase.delete(materialId));
        verify(stockServiceGateway, never()).deleteByMaterialId(any());
        verify(materialGateway, never()).deleteById(any());
    }
}
