package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialQuantity;
import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationResult;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTaskMaterialSnapshot;
import com.fiap.mechanical_hub.repair_service.infrastructure.messaging.SagaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StockReservationUseCase")
class StockReservationUseCaseTest {

    @Mock
    private OrderTaskGateway orderTaskGateway;
    @Mock
    private StockReservationGateway stockReservationGateway;
    @Mock
    private SagaEventPublisher publisher;

    @InjectMocks
    private StockReservationUseCase useCase;

    private UUID serviceOrderId;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        serviceOrderId = UUID.randomUUID();
        materialId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Agrega materiais de varias tarefas e publica material.reservado quando a reserva da certo")
    void shouldAggregateMaterialsAndPublishReservado() {
        OrderTask task1 = OrderTask.create(serviceOrderId, null,
                List.of(new OrderTaskMaterialSnapshot(materialId, "Óleo 5W30", 2)));
        OrderTask task2 = OrderTask.create(serviceOrderId, null,
                List.of(new OrderTaskMaterialSnapshot(materialId, "Óleo 5W30", 3)));

        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(task1, task2));
        when(stockReservationGateway.reserve(any(), any())).thenReturn(StockReservationResult.success());

        useCase.reserveForOrder(serviceOrderId);

        ArgumentCaptor<List<MaterialQuantity>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockReservationGateway).reserve(eq(serviceOrderId), captor.capture());
        List<MaterialQuantity> items = captor.getValue();
        assertEquals(1, items.size());
        assertEquals(materialId, items.get(0).materialId());
        assertEquals(5, items.get(0).quantity());

        verify(publisher).publishMaterialReservado(serviceOrderId);
        verify(publisher, never()).publishMaterialIndisponivel(any(), any());
    }

    @Test
    @DisplayName("Publica material.indisponivel quando a reserva falha")
    void shouldPublishIndisponivelWhenReservationFails() {
        OrderTask task = OrderTask.create(serviceOrderId, null,
                List.of(new OrderTaskMaterialSnapshot(materialId, "Filtro de óleo", 1)));

        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(task));
        when(stockReservationGateway.reserve(any(), any())).thenReturn(StockReservationResult.failure("sem peça em estoque"));

        useCase.reserveForOrder(serviceOrderId);

        verify(publisher).publishMaterialIndisponivel(serviceOrderId, "sem peça em estoque");
        verify(publisher, never()).publishMaterialReservado(any());
    }

    @Test
    @DisplayName("OS sem nenhuma tarefa/material ainda tenta reservar lista vazia e reporta sucesso")
    void shouldHandleOrderWithNoMaterials() {
        when(orderTaskGateway.findByServiceOrderId(serviceOrderId)).thenReturn(List.of());
        when(stockReservationGateway.reserve(serviceOrderId, List.of())).thenReturn(StockReservationResult.success());

        useCase.reserveForOrder(serviceOrderId);

        verify(stockReservationGateway).reserve(serviceOrderId, List.of());
        verify(publisher).publishMaterialReservado(serviceOrderId);
    }
}
