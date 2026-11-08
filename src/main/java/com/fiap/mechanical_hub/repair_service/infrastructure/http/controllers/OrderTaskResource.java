package com.fiap.mechanical_hub.repair_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.CreateOrderTaskRequest;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.OrderTaskResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.ReportExecutionFailureRequest;
import com.fiap.mechanical_hub.repair_service.application.dto.ordertask.UpdateTaskStatusRequest;
import com.fiap.mechanical_hub.repair_service.application.usecases.OrderTaskUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Transactional
@RestController
@RequestMapping("/service-orders/{serviceOrderId}/tasks")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tarefas de Execução", description = "Endpoints de diagnóstico/reparo de uma ordem de serviço")
public class OrderTaskResource {

    private final OrderTaskUseCase useCase;

    public OrderTaskResource(OrderTaskUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Adicionar tarefa à ordem",
            description = "Escolhe um serviço do catálogo pra aplicar nesta OS, criando a tarefa correspondente. " +
                    "Não reserva material nem calcula orçamento aqui (isso acontece depois, no pagamento confirmado " +
                    "e na geração do orçamento pelo billing-service). Requer perfil de Mecânico ou Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Serviço já adicionado a esta OS"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Mecânico ou Administrador)"),
            @ApiResponse(responseCode = "404", description = "Serviço ou material não encontrado no catálogo")
    })
    public ResponseEntity<OrderTaskResponse> create(
            @PathVariable UUID serviceOrderId,
            @RequestBody CreateOrderTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(serviceOrderId, request));
    }

    @GetMapping
    @Operation(summary = "Listar tarefas da ordem",
            description = "Retorna todas as tarefas de execução/diagnóstico desta OS. Também usado pelo " +
                    "billing-service pra saber o que foi selecionado e calcular o orçamento. Requer perfil de " +
                    "Mecânico ou Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tarefas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Mecânico ou Administrador)")
    })
    public ResponseEntity<List<OrderTaskResponse>> findByServiceOrderId(@PathVariable UUID serviceOrderId) {
        return ResponseEntity.ok(useCase.findByServiceOrderId(serviceOrderId));
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Atualizar status de uma tarefa",
            description = "Inicia ou finaliza uma tarefa (o material já foi confirmado reservado pra OS inteira " +
                    "antes da execução começar, não é checado de novo por tarefa). Quando a última tarefa pendente " +
                    "da OS é finalizada, publica execucao.finalizada pro orquestrador do Saga. Requer perfil de " +
                    "Mecânico ou Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição de status inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Mecânico ou Administrador)"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID serviceOrderId,
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskStatusRequest request) {
        useCase.updateStatus(serviceOrderId, taskId, request.status());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/execution-failure")
    @Operation(summary = "Reportar falha durante a execução",
            description = "Reporta que algo deu errado durante a execução manual já iniciada (não é sobre falta " +
                    "de material — isso já é reportado antes, via reserva de material). Publica execucao.falhou " +
                    "pro orquestrador do Saga, que volta a OS pra 'em diagnóstico'. Requer perfil de Mecânico ou " +
                    "Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Falha reportada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Mecânico ou Administrador)")
    })
    public ResponseEntity<Void> reportExecutionFailure(
            @PathVariable UUID serviceOrderId,
            @RequestBody ReportExecutionFailureRequest request) {
        useCase.reportExecutionFailure(serviceOrderId, request.motivo());
        return ResponseEntity.accepted().build();
    }
}
