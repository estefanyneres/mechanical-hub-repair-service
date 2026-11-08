package com.fiap.mechanical_hub.repair_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.repair_service.application.gateways.OrderTaskGateway;
import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers.OrderTaskDynamoMapper;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.OrderTaskModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderTaskGatewayImpl implements OrderTaskGateway {

    private final DynamoDbTable<OrderTaskModel> table;

    public OrderTaskGatewayImpl(DynamoDbTable<OrderTaskModel> orderTaskTable) {
        this.table = orderTaskTable;
    }

    @Override
    public OrderTask save(OrderTask orderTask) {
        OrderTaskModel model = OrderTaskDynamoMapper.toModel(orderTask);
        table.putItem(model);
        return orderTask;
    }

    @Override
    public Optional<OrderTask> findById(UUID serviceOrderId, UUID taskId) {
        OrderTaskModel model = table.getItem(Key.builder()
                .partitionValue("OS#" + serviceOrderId)
                .sortValue("ORDERTASK#" + taskId)
                .build());
        return Optional.ofNullable(model).map(OrderTaskDynamoMapper::toDomain);
    }

    @Override
    public List<OrderTask> findByServiceOrderId(UUID serviceOrderId) {
        return table.query(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue("OS#" + serviceOrderId)
                        .build()))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(OrderTaskDynamoMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByServiceId(UUID serviceId) {
        Expression filterExpression = Expression.builder()
                .expression("serviceId = :sid AND entityType = :et")
                .expressionValues(Map.of(
                        ":sid", AttributeValue.builder().s(serviceId.toString()).build(),
                        ":et", AttributeValue.builder().s(OrderTaskModel.ENTITY_TYPE).build()
                ))
                .build();

        return table.scan(ScanEnhancedRequest.builder().filterExpression(filterExpression).build())
                .stream()
                .anyMatch(page -> !page.items().isEmpty());
    }
}
