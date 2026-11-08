package com.fiap.mechanical_hub.repair_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.repair_service.application.dto.material.MaterialResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.material.UpsertMaterialRequest;
import com.fiap.mechanical_hub.repair_service.application.usecases.MaterialUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialResourceTest {

    @Mock
    private MaterialUseCase useCase;

    private MaterialResource resource;

    @BeforeEach
    void setUp() {
        resource = new MaterialResource(useCase);
    }

    @Test
    @DisplayName("Deve criar um material válido e devolver 201")
    void create_ShouldReturn201() {
        var request = new UpsertMaterialRequest("Óleo", "desc", BigDecimal.TEN, 5);
        var response = mock(MaterialResponse.class);
        when(useCase.create(request)).thenReturn(response);

        ResponseEntity<MaterialResponse> result = resource.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve rejeitar criação sem nome")
    void create_ShouldThrow_WhenNameBlank() {
        var request = new UpsertMaterialRequest(" ", "desc", BigDecimal.TEN, 5);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome");
    }

    @Test
    @DisplayName("Deve rejeitar criação sem descrição")
    void create_ShouldThrow_WhenDescriptionBlank() {
        var request = new UpsertMaterialRequest("nome", " ", BigDecimal.TEN, 5);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Descrição");
    }

    @Test
    @DisplayName("Deve rejeitar criação sem preço unitário")
    void create_ShouldThrow_WhenUnitPriceNull() {
        var request = new UpsertMaterialRequest("nome", "desc", null, 5);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preço unitário");
    }

    @Test
    @DisplayName("Deve rejeitar criação com preço unitário negativo")
    void create_ShouldThrow_WhenUnitPriceNegative() {
        var request = new UpsertMaterialRequest("nome", "desc", BigDecimal.valueOf(-1), 5);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preço unitário");
    }

    @Test
    @DisplayName("Deve rejeitar criação sem quantidade mínima em estoque")
    void create_ShouldThrow_WhenMinStockQuantityNull() {
        var request = new UpsertMaterialRequest("nome", "desc", BigDecimal.TEN, null);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínima em estoque");
    }

    @Test
    @DisplayName("Deve rejeitar criação com quantidade mínima em estoque menor que 1")
    void create_ShouldThrow_WhenMinStockQuantityLessThanOne() {
        var request = new UpsertMaterialRequest("nome", "desc", BigDecimal.TEN, 0);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínima em estoque");
    }

    @Test
    @DisplayName("Deve listar todos os materiais")
    void findAll_ShouldReturn200WithList() {
        var response = mock(MaterialResponse.class);
        when(useCase.findAll()).thenReturn(List.of(response));

        ResponseEntity<List<MaterialResponse>> result = resource.findAll();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    @DisplayName("Deve buscar um material por ID")
    void findById_ShouldReturn200() {
        UUID id = UUID.randomUUID();
        var response = mock(MaterialResponse.class);
        when(useCase.findMaterialById(id)).thenReturn(response);

        ResponseEntity<MaterialResponse> result = resource.findById(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve atualizar um material válido")
    void update_ShouldReturn200() {
        UUID id = UUID.randomUUID();
        var request = new UpsertMaterialRequest("novo nome", "desc", BigDecimal.TEN, 5);
        var response = mock(MaterialResponse.class);
        when(useCase.update(id, request)).thenReturn(response);

        ResponseEntity<MaterialResponse> result = resource.update(id, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve deletar um material e devolver 204")
    void delete_ShouldReturn204() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = resource.delete(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).delete(id);
    }
}
