package com.fiap.mechanical_hub.repair_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.service.UpsertServiceRequest;
import com.fiap.mechanical_hub.repair_service.application.usecases.ServiceUseCase;
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
class ServiceResourceTest {

    @Mock
    private ServiceUseCase useCase;

    private ServiceResource resource;

    @BeforeEach
    void setUp() {
        resource = new ServiceResource(useCase);
    }

    @Test
    @DisplayName("Deve criar um serviço válido e devolver 201")
    void create_ShouldReturn201() {
        var request = new UpsertServiceRequest("Troca de óleo", "desc", BigDecimal.TEN);
        var response = mock(ServiceResponse.class);
        when(useCase.create(request)).thenReturn(response);

        ResponseEntity<ServiceResponse> result = resource.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve rejeitar criação sem nome")
    void create_ShouldThrow_WhenNameBlank() {
        var request = new UpsertServiceRequest(" ", "desc", BigDecimal.TEN);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome");
        verify(useCase, never()).create(any());
    }

    @Test
    @DisplayName("Deve rejeitar criação sem descrição")
    void create_ShouldThrow_WhenDescriptionBlank() {
        var request = new UpsertServiceRequest("nome", " ", BigDecimal.TEN);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Descrição");
    }

    @Test
    @DisplayName("Deve rejeitar criação sem custo de mão de obra")
    void create_ShouldThrow_WhenLaborCostNull() {
        var request = new UpsertServiceRequest("nome", "desc", null);

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Custo de mão de obra");
    }

    @Test
    @DisplayName("Deve rejeitar criação com custo de mão de obra negativo")
    void create_ShouldThrow_WhenLaborCostNegative() {
        var request = new UpsertServiceRequest("nome", "desc", BigDecimal.valueOf(-1));

        assertThatThrownBy(() -> resource.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Custo de mão de obra");
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void findAll_ShouldReturn200WithList() {
        var response = mock(ServiceResponse.class);
        when(useCase.findAll()).thenReturn(List.of(response));

        ResponseEntity<List<ServiceResponse>> result = resource.findAll();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    @DisplayName("Deve buscar um serviço por ID")
    void findById_ShouldReturn200() {
        UUID id = UUID.randomUUID();
        var response = mock(ServiceResponse.class);
        when(useCase.findById(id)).thenReturn(response);

        ResponseEntity<ServiceResponse> result = resource.findById(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve atualizar um serviço válido")
    void update_ShouldReturn200() {
        UUID id = UUID.randomUUID();
        var request = new UpsertServiceRequest("novo nome", "desc", BigDecimal.TEN);
        var response = mock(ServiceResponse.class);
        when(useCase.update(id, request)).thenReturn(response);

        ResponseEntity<ServiceResponse> result = resource.update(id, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    @DisplayName("Deve deletar um serviço e devolver 204")
    void delete_ShouldReturn204() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = resource.delete(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).delete(id);
    }
}
