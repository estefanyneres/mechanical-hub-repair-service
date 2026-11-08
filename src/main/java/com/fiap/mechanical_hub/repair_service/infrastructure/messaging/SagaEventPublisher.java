package com.fiap.mechanical_hub.repair_service.infrastructure.messaging;

import com.fiap.mechanical_hub.repair_service.application.dto.saga.ExecucaoFalhouEvent;
import com.fiap.mechanical_hub.repair_service.application.dto.saga.ExecucaoFinalizadaEvent;
import com.fiap.mechanical_hub.repair_service.application.dto.saga.MaterialIndisponivelEvent;
import com.fiap.mechanical_hub.repair_service.application.dto.saga.MaterialReservadoEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SagaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SagaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMaterialReservado(UUID serviceOrderId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_MATERIAL_RESERVADO,
                new MaterialReservadoEvent(serviceOrderId));
    }

    public void publishMaterialIndisponivel(UUID serviceOrderId, String motivo) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_MATERIAL_INDISPONIVEL,
                new MaterialIndisponivelEvent(serviceOrderId, motivo));
    }

    public void publishExecucaoFinalizada(UUID serviceOrderId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_EXECUCAO_FINALIZADA,
                new ExecucaoFinalizadaEvent(serviceOrderId));
    }

    public void publishExecucaoFalhou(UUID serviceOrderId, String motivo) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_EXECUCAO_FALHOU,
                new ExecucaoFalhouEvent(serviceOrderId, motivo));
    }
}
