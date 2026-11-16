package com.fiap.mechanical_hub.repair_service.infrastructure.integrations.core;

import com.fiap.mechanical_hub.repair_service.application.gateways.CoreServiceGateway;
import com.fiap.mechanical_hub.repair_service.infrastructure.security.ServiceTokenIssuer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Chamada REST sincrona ao core-service (TASK-047), mesmo padrao ja usado
 * pro stock-service (StockReservationGatewayHttpClient): token de servico
 * proprio (ServiceTokenIssuer), reaproveitando o GET /service-orders/{id}/status
 * que ja existia (perfil ADMINISTRATOR tem acesso, ver SecurityConfiguration
 * do core-service).
 */
@Slf4j
@Component
public class CoreServiceGatewayHttpClient implements CoreServiceGateway {

    private static final String READY_FOR_EXECUTION_STATUS = "Pronto para execução";

    private final RestTemplate restTemplate;
    private final ServiceTokenIssuer serviceTokenIssuer;
    private final String coreServiceUrl;

    public CoreServiceGatewayHttpClient(RestTemplate restTemplate,
                                         ServiceTokenIssuer serviceTokenIssuer,
                                         @Value("${core-service.url}") String coreServiceUrl) {
        this.restTemplate = restTemplate;
        this.serviceTokenIssuer = serviceTokenIssuer;
        this.coreServiceUrl = coreServiceUrl;
    }

    @Override
    public boolean isReadyForExecution(UUID serviceOrderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceTokenIssuer.issue());

        var response = restTemplate.exchange(
                coreServiceUrl + "/service-orders/" + serviceOrderId + "/status",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ServiceOrderStatusResponseDto.class);

        ServiceOrderStatusResponseDto body = response.getBody();
        if (body == null) {
            log.warn("Resposta vazia do core-service ao consultar status da OS {}", serviceOrderId);
            return false;
        }

        return READY_FOR_EXECUTION_STATUS.equals(body.status());
    }
}
