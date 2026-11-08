package com.fiap.mechanical_hub.repair_service.domain.entities.mocks;

import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;

import java.util.List;

import static com.fiap.mechanical_hub.repair_service.domain.entities.constants.TestConstants.DEFAULT_SERVICE_ORDER_ID;

public class OrderTaskMock {

    public static OrderTask defaultOrderTask() {
        ServiceData service = ServiceMock.defaultService();
        return OrderTask.create(DEFAULT_SERVICE_ORDER_ID, service, List.of());
    }

    public static OrderTask orderTaskWithCustomService(ServiceData service) {
        return OrderTask.create(DEFAULT_SERVICE_ORDER_ID, service, List.of());
    }
}
