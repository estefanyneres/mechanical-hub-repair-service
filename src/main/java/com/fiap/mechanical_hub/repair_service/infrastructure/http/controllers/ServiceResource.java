package com.fiap.mechanical_hub.repair_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.service.UpsertServiceRequest;
import com.fiap.mechanical_hub.repair_service.application.usecases.ServiceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Transactional
@RestController
@RequestMapping("/services")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços disponíveis (catálogo)")
public class ServiceResource {

    private final ServiceUseCase useCase;

    public ServiceResource(ServiceUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Criar novo serviço", description = "Cria um novo tipo de serviço no catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Serviço criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<ServiceResponse> create(@RequestBody UpsertServiceRequest request) {
        validate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os serviços", description = "Retorna lista de todos os tipos de serviços do catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de serviços"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<List<ServiceResponse>> findAll() {
        return ResponseEntity.ok(useCase.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter serviço por ID", description = "Retorna os detalhes de um serviço específico. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ServiceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar serviço", description = "Atualiza os dados de um serviço do catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<ServiceResponse> update(@PathVariable UUID id, @RequestBody UpsertServiceRequest request) {
        validate(request);
        return ResponseEntity.ok(useCase.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar serviço", description = "Deleta um serviço do catálogo, se não estiver vinculado a nenhuma tarefa. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Serviço deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(UpsertServiceRequest request) {
        if (request.name() == null || request.name().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (request.description() == null || request.description().isBlank())
            throw new IllegalArgumentException("Descrição é obrigatória");
        if (request.laborCost() == null)
            throw new IllegalArgumentException("Custo de mão de obra é obrigatório");
        if (request.laborCost().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Custo de mão de obra deve ser maior ou igual a zero");
    }
}
