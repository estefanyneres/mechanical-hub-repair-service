package com.fiap.mechanical_hub.repair_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.repair_service.application.usecases.StockReservationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SagaEventListener")
class SagaEventListenerTest {

    @Mock
    private StockReservationUseCase stockReservationUseCase;

    private SagaEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SagaEventListener(stockReservationUseCase, new ObjectMapper());
    }

    @Test
    @DisplayName("Delega pro use case de reserva tanto em pagamento.confirmado quanto em estoque.reabastecido")
    void shouldDelegateToReservationUseCaseForBothRoutingKeys() throws Exception {
        UUID orderId = UUID.randomUUID();

        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_PAGAMENTO_CONFIRMADO,
                "{\"serviceOrderId\":\"" + orderId + "\"}"));
        listener.onMessage(message(RabbitMQConfig.ROUTING_KEY_ESTOQUE_REABASTECIDO,
                "{\"serviceOrderId\":\"" + orderId + "\"}"));

        verify(stockReservationUseCase, org.mockito.Mockito.times(2)).reserveForOrder(orderId);
    }

    private Message message(String routingKey, String json) {
        MessageProperties properties = new MessageProperties();
        properties.setReceivedRoutingKey(routingKey);
        return new Message(json.getBytes(StandardCharsets.UTF_8), properties);
    }
}
