package com.fiap.mechanical_hub.repair_service.domain.entities.constants;

import java.math.BigDecimal;
import java.util.UUID;

public class TestConstants {

    public static final UUID DEFAULT_MATERIAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    public static final String DEFAULT_MATERIAL_NAME = "Óleo de Motor";
    public static final String DEFAULT_MATERIAL_DESCRIPTION = "Óleo de motor sintético 5W30";
    public static final BigDecimal DEFAULT_MATERIAL_UNIT_PRICE = BigDecimal.valueOf(50.00);
    public static final Integer DEFAULT_MATERIAL_MIN_STOCK = 10;

    public static final UUID DEFAULT_SERVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");
    public static final String DEFAULT_SERVICE_NAME = "Troca de óleo";
    public static final String DEFAULT_SERVICE_DESCRIPTION = "Serviço de troca de óleo do motor";
    public static final BigDecimal DEFAULT_SERVICE_LABOR_COST = BigDecimal.valueOf(100.00);

    public static final UUID DEFAULT_SERVICE_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");

    public static final UUID DEFAULT_ORDER_TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000008");
}
