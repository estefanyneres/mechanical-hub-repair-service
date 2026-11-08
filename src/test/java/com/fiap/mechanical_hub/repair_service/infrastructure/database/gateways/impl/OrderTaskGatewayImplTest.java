package com.fiap.mechanical_hub.repair_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.repair_service.domain.entities.OrderTask;
import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.OrderTaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.ServiceMock.defaultService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTaskGatewayImplTest {

    @Mock
    private DynamoDbTable<OrderTaskModel> table;

    private OrderTaskGatewayImpl gateway;
    private UUID serviceOrderId;

    @BeforeEach
    void setUp() {
        gateway = new OrderTaskGatewayImpl(table);
        serviceOrderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve salvar a tarefa e devolver a mesma instância de domínio")
    void save_ShouldPutItemAndReturnDomain() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());

        OrderTask result = gateway.save(task);

        verify(table).putItem(any(OrderTaskModel.class));
        assertThat(result).isSameAs(task);
    }

    @Test
    @DisplayName("Deve encontrar a tarefa por ID, convertendo o model pra domínio")
    void findById_ShouldReturnDomain_WhenFound() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());
        OrderTaskModel model = toModel(task);

        when(table.getItem(any(Key.class))).thenReturn(model);

        Optional<OrderTask> result = gateway.findById(serviceOrderId, task.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(task.getId());
        assertThat(result.get().getServiceOrderId()).isEqualTo(serviceOrderId);
    }

    @Test
    @DisplayName("Deve devolver Optional vazio quando a tarefa não existe")
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        Optional<OrderTask> result = gateway.findById(serviceOrderId, UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todas as tarefas de uma OS via Query por PK")
    void findByServiceOrderId_ShouldReturnAllTasks() {
        ServiceData service = defaultService();
        OrderTask task1 = OrderTask.create(serviceOrderId, service, List.of());
        OrderTask task2 = OrderTask.create(serviceOrderId, service, List.of());

        Page<OrderTaskModel> page = mockPage(List.of(toModel(task1), toModel(task2)));
        PageIterable<OrderTaskModel> pageIterable = mock(PageIterable.class);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(table.query(any(QueryConditional.class))).thenReturn(pageIterable);

        List<OrderTask> result = gateway.findByServiceOrderId(serviceOrderId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Deve retornar true quando existe tarefa vinculada ao serviço (scan com filtro)")
    void existsByServiceId_ShouldReturnTrue_WhenTaskFound() {
        ServiceData service = defaultService();
        OrderTask task = OrderTask.create(serviceOrderId, service, List.of());

        Page<OrderTaskModel> page = mockPage(List.of(toModel(task)));
        PageIterable<OrderTaskModel> pageIterable = mock(PageIterable.class);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

        boolean result = gateway.existsByServiceId(service.getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando nenhuma tarefa está vinculada ao serviço")
    void existsByServiceId_ShouldReturnFalse_WhenNoTaskFound() {
        Page<OrderTaskModel> emptyPage = mockPage(List.of());
        PageIterable<OrderTaskModel> pageIterable = mock(PageIterable.class);
        when(pageIterable.stream()).thenReturn(Stream.of(emptyPage));
        when(table.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

        boolean result = gateway.existsByServiceId(UUID.randomUUID());

        assertThat(result).isFalse();
    }

    @SuppressWarnings("unchecked")
    private Page<OrderTaskModel> mockPage(List<OrderTaskModel> items) {
        Page<OrderTaskModel> page = mock(Page.class);
        when(page.items()).thenReturn(items);
        return page;
    }

    private OrderTaskModel toModel(OrderTask task) {
        return com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers.OrderTaskDynamoMapper.toModel(task);
    }
}
