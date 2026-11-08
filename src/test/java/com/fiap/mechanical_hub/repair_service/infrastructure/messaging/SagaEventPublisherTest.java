package com.fiap.mechanical_hub.repair_service.infrastructure.messaging;

import com.fiap.mechanical_hub.repair_service.application.dto.saga.ExecucaoFalhouEvent;
import com.fiap.mechanical_hub.repair_service.application.dto.saga.ExecucaoFinalizadaEvent;
import com.fiap.mechanical_hub.repair_service.application.dto.saga.MaterialIndisponivelEvent;
import com.fiap.mechanical_hub.repair_service.application.dto.saga.MaterialReservadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SagaEventPublisher")
class SagaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private SagaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SagaEventPublisher(rabbitTemplate);
    }

    @Test
    @DisplayName("Publica material.reservado na exchange do saga")
    void shouldPublishMaterialReservado() {
        UUID serviceOrderId = UUID.randomUUID();

        publisher.publishMaterialReservado(serviceOrderId);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.SAGA_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_MATERIAL_RESERVADO,
                new MaterialReservadoEvent(serviceOrderId));
    }

    @Test
    @DisplayName("Publica material.indisponivel com o motivo na exchange do saga")
    void shouldPublishMaterialIndisponivel() {
        UUID serviceOrderId = UUID.randomUUID();

        publisher.publishMaterialIndisponivel(serviceOrderId, "sem peça em estoque");

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.SAGA_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_MATERIAL_INDISPONIVEL,
                new MaterialIndisponivelEvent(serviceOrderId, "sem peça em estoque"));
    }

    @Test
    @DisplayName("Publica execucao.finalizada na exchange do saga")
    void shouldPublishExecucaoFinalizada() {
        UUID serviceOrderId = UUID.randomUUID();

        publisher.publishExecucaoFinalizada(serviceOrderId);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.SAGA_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_EXECUCAO_FINALIZADA,
                new ExecucaoFinalizadaEvent(serviceOrderId));
    }

    @Test
    @DisplayName("Publica execucao.falhou com o motivo na exchange do saga")
    void shouldPublishExecucaoFalhou() {
        UUID serviceOrderId = UUID.randomUUID();

        publisher.publishExecucaoFalhou(serviceOrderId, "problema maior que o diagnosticado");

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.SAGA_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_EXECUCAO_FALHOU,
                new ExecucaoFalhouEvent(serviceOrderId, "problema maior que o diagnosticado"));
    }
}
