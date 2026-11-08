package com.fiap.mechanical_hub.repair_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.repair_service.application.usecases.StockReservationUseCase;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Le o JSON bruto em vez do routing key pra decidir o tipo (mesmo motivo
 * do SagaEventListener do core-service: nao acoplar ao payload de outro
 * servico) -- mas aqui os dois eventos que este servico consome
 * (pagamento.confirmado e estoque.reabastecido) disparam exatamente a
 * mesma acao, entao nem precisa de um switch.
 */
@Component
public class SagaEventListener {

    private final StockReservationUseCase stockReservationUseCase;
    private final ObjectMapper objectMapper;

    public SagaEventListener(StockReservationUseCase stockReservationUseCase, ObjectMapper objectMapper) {
        this.stockReservationUseCase = stockReservationUseCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.REPAIR_SERVICE_QUEUE)
    public void onMessage(Message message) throws IOException {
        JsonNode body = objectMapper.readTree(message.getBody());
        UUID serviceOrderId = UUID.fromString(body.get("serviceOrderId").asText());
        stockReservationUseCase.reserveForOrder(serviceOrderId);
    }
}
