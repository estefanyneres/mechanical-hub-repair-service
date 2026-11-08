package com.fiap.mechanical_hub.repair_service.infrastructure.config;

import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.MaterialModel;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.OrderTaskModel;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.ServiceModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Sem credenciais explicitas: usa a default credential chain do SDK, que
 * no EKS herda o papel da instancia do node (LabRole) via IMDS -- mesmo
 * mecanismo que ja autentica o kubectl/CLI nos outros repositorios deste
 * projeto.
 *
 * Single-table design (TASK-021): as 3 tabelas logicas (materiais,
 * servicos do catalogo, tarefas de execucao) vivem na mesma tabela fisica
 * "mechanical-hub-repair-service", diferenciadas pelo prefixo da PK.
 */
@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${dynamodb.table-name}")
    private String tableName;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<MaterialModel> materialTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tableName, TableSchema.fromBean(MaterialModel.class));
    }

    @Bean
    public DynamoDbTable<ServiceModel> serviceTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tableName, TableSchema.fromBean(ServiceModel.class));
    }

    @Bean
    public DynamoDbTable<OrderTaskModel> orderTaskTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(tableName, TableSchema.fromBean(OrderTaskModel.class));
    }
}
