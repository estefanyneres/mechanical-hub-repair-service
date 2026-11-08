package com.fiap.mechanical_hub.repair_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.CreateOrderTaskRequest;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.ReportExecutionFailureRequest;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.UpdateTaskStatusRequest;
import com.fiap.mechanical_hub.repair_service.application.usecases.OrderTaskUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTaskResourceTest {

    @Mock
    private OrderTaskUseCase useCase;

    private OrderTaskResource resource;
    private UUID serviceOrderId;

    @BeforeEach
    void setUp() {
        resource = new OrderTaskResource(useCase);
        serviceOrderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve criar uma tarefa e devolver 201")
    void create_ShouldReturn201() {
        var request = new CreateOrderTaskRequest(UUID.randomUUID(), null);
        var response = mock(OrderTaskResponse.class);
        when(useCase.create(serviceOrderId, request)).thenReturn(response);

        ResponseEntity<OrderTaskResponse> result = resource.create(serviceOrderId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve listar as tarefas de uma OS")
    void findByServiceOrderId_ShouldReturn200WithList() {
        var response = mock(OrderTaskResponse.class);
        when(useCase.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(response));

        ResponseEntity<List<OrderTaskResponse>> result = resource.findByServiceOrderId(serviceOrderId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    @DisplayName("Deve atualizar o status de uma tarefa e devolver 204")
    void updateStatus_ShouldReturn204() {
        UUID taskId = UUID.randomUUID();
        var request = new UpdateTaskStatusRequest("INICIADO");

        ResponseEntity<Void> result = resource.updateStatus(serviceOrderId, taskId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).updateStatus(serviceOrderId, taskId, "INICIADO");
    }

    @Test
    @DisplayName("Deve reportar falha na execução e devolver 202")
    void reportExecutionFailure_ShouldReturn202() {
        var request = new ReportExecutionFailureRequest("motivo qualquer");

        ResponseEntity<Void> result = resource.reportExecutionFailure(serviceOrderId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(useCase).reportExecutionFailure(serviceOrderId, "motivo qualquer");
    }
}
