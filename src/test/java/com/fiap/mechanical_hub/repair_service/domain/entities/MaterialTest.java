package com.fiap.mechanical_hub.repair_service.domain.entities;

import com.fiap.mechanical_hub.repair_service.domain.entities.constants.TestConstants;
import com.fiap.mechanical_hub.repair_service.domain.entities.mocks.MaterialMock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {

    @Test
    void shouldCreateMaterialWithValidData() {
        Material material = MaterialMock.defaultMaterial();

        assertNotNull(material.getId());
        assertEquals(TestConstants.DEFAULT_MATERIAL_NAME, material.getName());
        assertEquals(TestConstants.DEFAULT_MATERIAL_DESCRIPTION, material.getDescription());
        assertEquals(TestConstants.DEFAULT_MATERIAL_UNIT_PRICE, material.getUnitPrice());
        assertEquals(TestConstants.DEFAULT_MATERIAL_MIN_STOCK, material.getMinStockQuantity());
        assertNotNull(material.getCreatedAt());
        assertNotNull(material.getUpdatedAt());
    }

    @Test
    void shouldUpdateMaterial() {
        Material material = MaterialMock.defaultMaterial();

        material.update("Óleo Premium", "Óleo premium 10W40", BigDecimal.valueOf(75.00), 20);

        assertEquals("Óleo Premium", material.getName());
        assertEquals("Óleo premium 10W40", material.getDescription());
        assertEquals(BigDecimal.valueOf(75.00), material.getUnitPrice());
        assertEquals(20, material.getMinStockQuantity());
    }

    @Test
    void shouldCreateMaterialWithHighPrice() {
        Material material = MaterialMock.materialWithHighPrice();

        assertTrue(material.getUnitPrice().compareTo(TestConstants.DEFAULT_MATERIAL_UNIT_PRICE) > 0);
    }

    @Test
    void shouldCreateMaterialWithLowMinStock() {
        Material material = MaterialMock.materialWithLowMinStock();

        assertEquals(1, material.getMinStockQuantity());
    }

    @Test
    void shouldKeepIdImmutable() {
        Material material = MaterialMock.defaultMaterial();
        java.util.UUID originalId = material.getId();

        material.update("New Name", "New Description", BigDecimal.TEN, 5);

        assertEquals(originalId, material.getId());
    }

    @Test
    void shouldKeepCreatedAtImmutable() {
        Material material = MaterialMock.defaultMaterial();
        java.time.LocalDateTime originalCreatedAt = material.getCreatedAt();

        material.update("New Name", "New Description", BigDecimal.TEN, 5);

        assertEquals(originalCreatedAt, material.getCreatedAt());
    }
}
