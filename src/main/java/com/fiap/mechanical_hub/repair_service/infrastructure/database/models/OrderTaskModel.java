package com.fiap.mechanical_hub.repair_service.infrastructure.database.models;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.ArrayList;
import java.util.List;

@DynamoDbBean
public class OrderTaskModel {

    public static final String ENTITY_TYPE = "ORDERTASK";

    private String pk;
    private String sk;
    private String entityType;
    private String id;
    private String serviceOrderId;
    private String serviceId;
    private String serviceName;
    private String status;
    private String startedAt;
    private String finishedAt;
    private List<OrderTaskMaterialItem> materials = new ArrayList<>();

    @DynamoDbPartitionKey
    public String getPk() { return pk; }
    public void setPk(String pk) { this.pk = pk; }

    @DynamoDbSortKey
    @DynamoDbSecondarySortKey(indexNames = "EntityTypeIndex")
    public String getSk() { return sk; }
    public void setSk(String sk) { this.sk = sk; }

    @DynamoDbSecondaryPartitionKey(indexNames = "EntityTypeIndex")
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getServiceOrderId() { return serviceOrderId; }
    public void setServiceOrderId(String serviceOrderId) { this.serviceOrderId = serviceOrderId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public String getFinishedAt() { return finishedAt; }
    public void setFinishedAt(String finishedAt) { this.finishedAt = finishedAt; }

    public List<OrderTaskMaterialItem> getMaterials() { return materials; }
    public void setMaterials(List<OrderTaskMaterialItem> materials) { this.materials = materials; }
}
