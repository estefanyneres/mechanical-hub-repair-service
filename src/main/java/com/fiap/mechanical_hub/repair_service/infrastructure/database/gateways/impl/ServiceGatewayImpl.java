package com.fiap.mechanical_hub.repair_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.repair_service.application.gateways.ServiceGateway;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers.ServiceDynamoMapper;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.ServiceModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServiceGatewayImpl implements ServiceGateway {

    private final DynamoDbTable<ServiceModel> table;

    public ServiceGatewayImpl(DynamoDbTable<ServiceModel> serviceTable) {
        this.table = serviceTable;
    }

    @Override
    public Optional<ServiceData> findById(UUID id) {
        ServiceModel model = table.getItem(Key.builder()
                .partitionValue("SERVICE#" + id)
                .sortValue(ServiceModel.SK_VALUE)
                .build());
        return Optional.ofNullable(model).map(ServiceDynamoMapper::toDomain);
    }

    @Override
    public ServiceData save(ServiceData serviceData) {
        ServiceModel model = ServiceDynamoMapper.toModel(serviceData);
        table.putItem(model);
        return serviceData;
    }

    @Override
    public List<ServiceData> findAll() {
        DynamoDbIndex<ServiceModel> index = table.index("EntityTypeIndex");
        return index.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(ServiceModel.ENTITY_TYPE).build()))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(ServiceDynamoMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        table.deleteItem(Key.builder()
                .partitionValue("SERVICE#" + id)
                .sortValue(ServiceModel.SK_VALUE)
                .build());
    }

    @Override
    public List<ServiceData> findByIds(List<UUID> serviceIds) {
        return serviceIds.stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }
}
