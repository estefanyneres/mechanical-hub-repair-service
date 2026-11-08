package com.fiap.mechanical_hub.repair_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.repair_service.application.gateways.MaterialGateway;
import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers.MaterialDynamoMapper;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.MaterialModel;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MaterialGatewayImpl implements MaterialGateway {

    private final DynamoDbTable<MaterialModel> table;

    public MaterialGatewayImpl(DynamoDbTable<MaterialModel> materialTable) {
        this.table = materialTable;
    }

    @Override
    public Material save(Material material) {
        MaterialModel model = MaterialDynamoMapper.toModel(material);
        table.putItem(model);
        return material;
    }

    @Override
    public Optional<Material> findById(UUID id) {
        MaterialModel model = table.getItem(Key.builder()
                .partitionValue("MATERIAL#" + id)
                .sortValue(MaterialModel.SK_VALUE)
                .build());
        return Optional.ofNullable(model).map(MaterialDynamoMapper::toDomain);
    }

    @Override
    public List<Material> findAll() {
        DynamoDbIndex<MaterialModel> index = table.index("EntityTypeIndex");
        return index.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(MaterialModel.ENTITY_TYPE).build()))
                .stream()
                .flatMap(page -> page.items().stream())
                .map(MaterialDynamoMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        table.deleteItem(Key.builder()
                .partitionValue("MATERIAL#" + id)
                .sortValue(MaterialModel.SK_VALUE)
                .build());
    }
}
