package com.fiap.mechanical_hub.repair_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.repair_service.domain.entities.ServiceData;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers.ServiceDynamoMapper;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.ServiceModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.ServiceMock.defaultService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceGatewayImplTest {

    @Mock
    private DynamoDbTable<ServiceModel> table;
    @Mock
    private DynamoDbIndex<ServiceModel> index;

    private ServiceGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new ServiceGatewayImpl(table);
    }

    @Test
    @DisplayName("Deve salvar o serviço e devolver a mesma instância de domínio")
    void save_ShouldPutItemAndReturnDomain() {
        ServiceData service = defaultService();

        ServiceData result = gateway.save(service);

        verify(table).putItem(any(ServiceModel.class));
        assertThat(result).isSameAs(service);
    }

    @Test
    @DisplayName("Deve encontrar o serviço por ID")
    void findById_ShouldReturnDomain_WhenFound() {
        ServiceData service = defaultService();
        when(table.getItem(any(Key.class))).thenReturn(ServiceDynamoMapper.toModel(service));

        Optional<ServiceData> result = gateway.findById(service.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(service.getId());
        assertThat(result.get().getName()).isEqualTo(service.getName());
    }

    @Test
    @DisplayName("Deve devolver Optional vazio quando o serviço não existe")
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        Optional<ServiceData> result = gateway.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todo o catálogo via GSI EntityTypeIndex")
    void findAll_ShouldQueryEntityTypeIndex() {
        ServiceData service = defaultService();
        Page<ServiceModel> page = mockPage(List.of(ServiceDynamoMapper.toModel(service)));
        PageIterable<ServiceModel> pageIterable = mock(PageIterable.class);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(table.index("EntityTypeIndex")).thenReturn(index);
        when(index.query(any(QueryConditional.class))).thenReturn(pageIterable);

        List<ServiceData> result = gateway.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(service.getId());
    }

    @Test
    @DisplayName("Deve deletar o serviço pelo ID")
    void deleteById_ShouldDeleteItem() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(table).deleteItem(any(Key.class));
    }

    @Test
    @DisplayName("Deve buscar vários serviços por lista de IDs, ignorando os não encontrados")
    void findByIds_ShouldReturnOnlyFoundOnes() {
        ServiceData service = defaultService();
        UUID missingId = UUID.randomUUID();

        when(table.getItem(any(Key.class)))
                .thenReturn(ServiceDynamoMapper.toModel(service))
                .thenReturn(null);

        List<ServiceData> result = gateway.findByIds(List.of(service.getId(), missingId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(service.getId());
    }

    @SuppressWarnings("unchecked")
    private Page<ServiceModel> mockPage(List<ServiceModel> items) {
        Page<ServiceModel> page = mock(Page.class);
        when(page.items()).thenReturn(items);
        return page;
    }
}
