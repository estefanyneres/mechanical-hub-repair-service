package com.fiap.mechanical_hub.repair_service.infrastructure.config;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.ServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockReservationGateway;
import com.fiap.mechanical_hub.repair_service.application.gateways.StockServiceGateway;
import com.fiap.mechanical_hub.repair_service.application.presenters.MaterialPresenter;
import com.fiap.mechanical_hub.repair_service.application.presenters.OrderTaskPresenter;
import com.fiap.mechanical_hub.repair_service.application.presenters.ServicePresenter;
import com.fiap.mechanical_hub.repair_service.application.usecases.MaterialUseCase;
import com.fiap.mechanical_hub.repair_service.application.usecases.OrderTaskUseCase;
import com.fiap.mechanical_hub.repair_service.application.usecases.ServiceUseCase;
import com.fiap.mechanical_hub.repair_service.application.usecases.StockReservationUseCase;
import com.fiap.mechanical_hub.repair_service.infrastructure.messaging.SagaEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Casos de uso sao POJOs simples (sem @Component), seguindo o mesmo
 * padrao de composicao manual ja usado no core-service -- precisa de um
 * @Bean explicito aqui porque os controllers/listener (@Component)
 * precisam injetar uma instancia gerenciada pelo Spring.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public ServiceUseCase serviceUseCase(
            ServiceGateway serviceGateway,
            OrderTaskGateway orderTaskGateway,
            ServicePresenter servicePresenter) {
        return new ServiceUseCase(serviceGateway, orderTaskGateway, servicePresenter);
    }

    @Bean
    public MaterialUseCase materialUseCase(
            MaterialGateway materialGateway,
            StockServiceGateway stockServiceGateway,
            MaterialPresenter materialPresenter) {
        return new MaterialUseCase(materialGateway, stockServiceGateway, materialPresenter);
    }

    @Bean
    public OrderTaskUseCase orderTaskUseCase(
            OrderTaskGateway orderTaskGateway,
            ServiceUseCase serviceUseCase,
            MaterialUseCase materialUseCase,
            OrderTaskPresenter orderTaskPresenter,
            SagaEventPublisher publisher,
            StockReservationGateway stockReservationGateway) {
        return new OrderTaskUseCase(orderTaskGateway, serviceUseCase, materialUseCase, orderTaskPresenter,
                publisher, stockReservationGateway);
    }

    @Bean
    public StockReservationUseCase stockReservationUseCase(
            OrderTaskGateway orderTaskGateway,
            StockReservationGateway stockReservationGateway,
            SagaEventPublisher publisher) {
        return new StockReservationUseCase(orderTaskGateway, stockReservationGateway, publisher);
    }
}
