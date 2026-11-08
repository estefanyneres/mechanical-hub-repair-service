package com.fiap.mechanical_hub.repair_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.material.UpsertMaterialRequest;
import com.fiap.mechanical_hub.repair_service.application.usecases.MaterialUseCase;
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
@RequestMapping("/materials")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Materiais", description = "Endpoints para gerenciamento do catálogo de materiais/peças")
public class MaterialResource {

    private final MaterialUseCase useCase;

    public MaterialResource(MaterialUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Operation(summary = "Criar novo material", description = "Cria um novo material/peça no catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Material criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<MaterialResponse> create(@RequestBody UpsertMaterialRequest request) {
        validate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os materiais", description = "Retorna lista de todos os materiais do catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de materiais"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)")
    })
    public ResponseEntity<List<MaterialResponse>> findAll() {
        return ResponseEntity.ok(useCase.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter material por ID", description = "Retorna os detalhes de um material específico. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Material não encontrado")
    })
    public ResponseEntity<MaterialResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.findMaterialById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar material", description = "Atualiza os dados de um material do catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Material atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Material não encontrado")
    })
    public ResponseEntity<MaterialResponse> update(@PathVariable UUID id, @RequestBody UpsertMaterialRequest request) {
        validate(request);
        return ResponseEntity.ok(useCase.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar material", description = "Deleta um material do catálogo. Requer perfil de Administrador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Material deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer Administrador)"),
            @ApiResponse(responseCode = "404", description = "Material não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(UpsertMaterialRequest request) {
        if (request.name() == null || request.name().isBlank())
            throw new IllegalArgumentException("Nome é obrigatório");
        if (request.description() == null || request.description().isBlank())
            throw new IllegalArgumentException("Descrição é obrigatória");
        if (request.unitPrice() == null)
            throw new IllegalArgumentException("Preço unitário é obrigatório");
        if (request.unitPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Preço unitário deve ser maior ou igual a zero");
        if (request.minStockQuantity() == null)
            throw new IllegalArgumentException("Quantidade mínima em estoque é obrigatória");
        if (request.minStockQuantity() < 1)
            throw new IllegalArgumentException("Quantidade mínima em estoque deve ser pelo menos 1");
    }
}
