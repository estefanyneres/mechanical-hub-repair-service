package com.fiap.mechanical_hub.repair_service.domain.entities;

import com.fiap.mechanical_hub.repair_service.domain.entities.constants.TestConstants;
import com.fiap.mechanical_hub.repair_service.domain.entities.mocks.ServiceMock;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServiceDataTest {

    @Test
    void shouldCreateServiceWithValidData() {
        ServiceData service = ServiceMock.defaultService();

        assertNotNull(service.getId());
        assertEquals(TestConstants.DEFAULT_SERVICE_NAME, service.getName());
        assertEquals(TestConstants.DEFAULT_SERVICE_DESCRIPTION, service.getDescription());
        assertEquals(TestConstants.DEFAULT_SERVICE_LABOR_COST, service.getLaborCost());
        assertTrue(service.isActive());
        assertNotNull(service.getTotalPrice());
        assertNotNull(service.getDefaultMaterials());
        assertTrue(service.getDefaultMaterials().isEmpty());
    }

    @Test
    void shouldCalculateTotalPriceAsLaborCost() {
        ServiceData service = ServiceMock.defaultService();

        assertEquals(TestConstants.DEFAULT_SERVICE_LABOR_COST, service.getTotalPrice());
    }

    @Test
    void shouldRecalculateTotalPriceWhenUpdating() {
        ServiceData service = ServiceMock.defaultService();
        BigDecimal originalPrice = service.getTotalPrice();

        service.update("Serviço Atualizado", "Descrição atualizada", BigDecimal.valueOf(200.00));

        assertNotEquals(originalPrice, service.getTotalPrice());
    }

    @Test
    void shouldDeactivateService() {
        ServiceData service = ServiceMock.defaultService();
        assertTrue(service.isActive());

        service.deactivate();

        assertFalse(service.isActive());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithBlankName() {
        assertThrows(BusinessRuleException.class, () ->
                ServiceData.create("", TestConstants.DEFAULT_SERVICE_DESCRIPTION, TestConstants.DEFAULT_SERVICE_LABOR_COST));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullName() {
        assertThrows(BusinessRuleException.class, () ->
                ServiceData.create(null, TestConstants.DEFAULT_SERVICE_DESCRIPTION, TestConstants.DEFAULT_SERVICE_LABOR_COST));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNegativeLaborCost() {
        assertThrows(BusinessRuleException.class, () ->
                ServiceData.create(TestConstants.DEFAULT_SERVICE_NAME, TestConstants.DEFAULT_SERVICE_DESCRIPTION,
                        BigDecimal.valueOf(-50.00)));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullLaborCost() {
        assertThrows(BusinessRuleException.class, () ->
                ServiceData.create(TestConstants.DEFAULT_SERVICE_NAME, TestConstants.DEFAULT_SERVICE_DESCRIPTION, null));
    }

    @Test
    void shouldKeepIdImmutable() {
        ServiceData service = ServiceMock.defaultService();
        java.util.UUID originalId = service.getId();

        service.deactivate();

        assertEquals(originalId, service.getId());
    }

    @Test
    void shouldCreateServiceWithNoArgsConstructor() {
        ServiceData service = new ServiceData();

        assertNull(service.getId());
        assertNull(service.getName());
        assertFalse(service.isActive());
        assertNotNull(service.getDefaultMaterials());
        assertTrue(service.getDefaultMaterials().isEmpty());
    }

    @Test
    void shouldSetDefaultMaterials() {
        ServiceData service = ServiceMock.defaultService();
        var requirement = new MaterialRequirement(TestConstants.DEFAULT_MATERIAL_ID, 2);

        service.setDefaultMaterials(java.util.List.of(requirement));

        assertEquals(1, service.getDefaultMaterials().size());
        assertEquals(TestConstants.DEFAULT_MATERIAL_ID, service.getDefaultMaterials().get(0).materialId());
    }
}
