package com.fiap.mechanical_hub.repair_service.application.usecases;

import com.fiap.mechanical_hub.repair_service.application.dto.service.ServiceResponse;
import com.fiap.mechanical_hub.repair_service.application.dto.service.UpsertServiceRequest;
import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.ServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.presenters.ServicePresenter;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.repair_service.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUseCaseTest {

    @Mock
    private ServiceGateway serviceGateway;

    @Mock
    private OrderTaskGateway orderTaskGateway;

    @Mock
    private ServicePresenter servicePresenter;

    @InjectMocks
    private ServiceUseCase serviceUseCase;

    @Test
    @DisplayName("Deve criar um serviço com sucesso")
    void create_ShouldReturnServiceData() {
        var request = new UpsertServiceRequest("Troca de Óleo", "Serviço padrão", new BigDecimal("80.00"));
        ServiceResponse expectedResponse = new ServiceResponse(null, "Troca de Óleo", "Serviço padrão",
                new BigDecimal("80.00"), new BigDecimal("80.00"), true, null, null);

        when(servicePresenter.toResponse(any(ServiceData.class))).thenReturn(expectedResponse);

        var response = serviceUseCase.create(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Troca de Óleo");
        verify(serviceGateway, times(1)).save(any(ServiceData.class));
        verify(servicePresenter).toResponse(any(ServiceData.class));
    }

    @Test
    @DisplayName("Deve atualizar um serviço existente com sucesso")
    void update_ShouldModifyExistingService() {
        UUID serviceId = UUID.randomUUID();
        ServiceData existingService = ServiceData.create("Nome Antigo", "Desc", BigDecimal.TEN);
        var request = new UpsertServiceRequest("Nome Novo", "Nova Desc", BigDecimal.ZERO);

        ServiceResponse expectedResponse = new ServiceResponse(serviceId, "Nome Novo", "Nova Desc",
                BigDecimal.ZERO, BigDecimal.ZERO, true, null, null);
        when(serviceGateway.findById(serviceId)).thenReturn(Optional.of(existingService));
        when(servicePresenter.toResponse(existingService)).thenReturn(expectedResponse);

        var response = serviceUseCase.update(serviceId, request);

        assertThat(response.name()).isEqualTo("Nome Novo");
        verify(serviceGateway).save(existingService);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao tentar atualizar serviço inexistente")
    void update_ShouldThrowNotFound_WhenIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(serviceGateway.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceUseCase.update(id, mock(UpsertServiceRequest.class)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Serviço não encontrado");
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void findById_ShouldReturnResponse() {
        UUID id = UUID.randomUUID();
        ServiceData serviceData = ServiceData.create("Teste", "Desc", BigDecimal.TEN);
        ServiceResponse expectedResponse = new ServiceResponse(id, "Teste", "Desc", BigDecimal.TEN, BigDecimal.TEN, true, null, null);
        when(serviceGateway.findById(id)).thenReturn(Optional.of(serviceData));
        when(servicePresenter.toResponse(serviceData)).thenReturn(expectedResponse);

        var response = serviceUseCase.findById(id);

        assertThat(response.name()).isEqualTo("Teste");
    }

    @Test
    @DisplayName("Deve retornar lista de todos os serviços")
    void findAll_ShouldReturnList() {
        ServiceData s1 = ServiceData.create("S1", "D1", BigDecimal.ONE);
        ServiceResponse r1 = new ServiceResponse(null, "S1", "D1", BigDecimal.ONE, BigDecimal.ONE, true, null, null);
        when(serviceGateway.findAll()).thenReturn(List.of(s1));
        when(servicePresenter.toResponseList(List.of(s1))).thenReturn(List.of(r1));

        var result = serviceUseCase.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar serviço com sucesso quando não vinculado a ordens")
    void delete_ShouldInvokeRepository() {
        UUID id = UUID.randomUUID();
        when(orderTaskGateway.existsByServiceId(id)).thenReturn(false);

        serviceUseCase.delete(id);

        verify(serviceGateway, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao deletar serviço vinculado a ordem")
    void delete_ShouldThrowWhenServiceLinkedToOrder() {
        UUID id = UUID.randomUUID();
        when(orderTaskGateway.existsByServiceId(id)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> serviceUseCase.delete(id));

        verify(serviceGateway, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve retornar ServiceData quando o ID existir")
    void findServiceById_Success() {
        UUID serviceId = UUID.randomUUID();
        ServiceData serviceData = mock(ServiceData.class);
        when(serviceGateway.findById(serviceId)).thenReturn(Optional.of(serviceData));

        ServiceData result = serviceUseCase.findServiceById(serviceId);

        assertEquals(serviceData, result);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando o ID do serviço não existir")
    void findServiceById_NotFound() {
        UUID serviceId = UUID.randomUUID();
        when(serviceGateway.findById(serviceId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> serviceUseCase.findServiceById(serviceId));
    }

    @Test
    @DisplayName("Deve retornar lista de ServiceData ao buscar por lista de IDs")
    void findAllByIds_Success() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        ServiceData s1 = mock(ServiceData.class);
        ServiceData s2 = mock(ServiceData.class);
        when(serviceGateway.findByIds(ids)).thenReturn(List.of(s1, s2));

        List<ServiceData> result = serviceUseCase.findAll(ids);

        assertEquals(2, result.size());
    }
}
