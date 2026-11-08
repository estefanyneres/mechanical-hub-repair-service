package com.fiap.mechanical_hub.repair_service.domain.entities.mocks;

import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;

import java.math.BigDecimal;

import static com.fiap.mechanical_hub.repair_service.domain.entities.constants.TestConstants.*;

public class ServiceMock {

    public static ServiceData defaultService() {
        return ServiceData.create(
                DEFAULT_SERVICE_NAME,
                DEFAULT_SERVICE_DESCRIPTION,
                DEFAULT_SERVICE_LABOR_COST
        );
    }

    public static ServiceData serviceWithHighPrice() {
        return ServiceData.create(
                "Serviço Premium",
                "Serviço de alta qualidade",
                BigDecimal.valueOf(500.00)
        );
    }
}
