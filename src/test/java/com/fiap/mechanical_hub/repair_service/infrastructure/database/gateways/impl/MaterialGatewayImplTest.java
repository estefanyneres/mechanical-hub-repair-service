package com.fiap.mechanical_hub.repair_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.repair_service.domain.entities.Material;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.mappers.MaterialDynamoMapper;
import com.fiap.mechanical_hub.repair_service.infrastructure.database.models.MaterialModel;
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

import static com.fiap.mechanical_hub.repair_service.domain.entities.mocks.MaterialMock.defaultMaterial;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialGatewayImplTest {

    @Mock
    private DynamoDbTable<MaterialModel> table;
    @Mock
    private DynamoDbIndex<MaterialModel> index;

    private MaterialGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new MaterialGatewayImpl(table);
    }

    @Test
    @DisplayName("Deve salvar o material e devolver a mesma instância de domínio")
    void save_ShouldPutItemAndReturnDomain() {
        Material material = defaultMaterial();

        Material result = gateway.save(material);

        verify(table).putItem(any(MaterialModel.class));
        assertThat(result).isSameAs(material);
    }

    @Test
    @DisplayName("Deve encontrar o material por ID")
    void findById_ShouldReturnDomain_WhenFound() {
        Material material = defaultMaterial();
        when(table.getItem(any(Key.class))).thenReturn(MaterialDynamoMapper.toModel(material));

        Optional<Material> result = gateway.findById(material.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(material.getId());
        assertThat(result.get().getName()).isEqualTo(material.getName());
    }

    @Test
    @DisplayName("Deve devolver Optional vazio quando o material não existe")
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(table.getItem(any(Key.class))).thenReturn(null);

        Optional<Material> result = gateway.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todo o catálogo de materiais via GSI EntityTypeIndex")
    void findAll_ShouldQueryEntityTypeIndex() {
        Material material = defaultMaterial();
        Page<MaterialModel> page = mockPage(List.of(MaterialDynamoMapper.toModel(material)));
        PageIterable<MaterialModel> pageIterable = mock(PageIterable.class);
        when(pageIterable.stream()).thenReturn(Stream.of(page));
        when(table.index("EntityTypeIndex")).thenReturn(index);
        when(index.query(any(QueryConditional.class))).thenReturn(pageIterable);

        List<Material> result = gateway.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(material.getId());
    }

    @Test
    @DisplayName("Deve deletar o material pelo ID")
    void deleteById_ShouldDeleteItem() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(table).deleteItem(any(Key.class));
    }

    @SuppressWarnings("unchecked")
    private Page<MaterialModel> mockPage(List<MaterialModel> items) {
        Page<MaterialModel> page = mock(Page.class);
        when(page.items()).thenReturn(items);
        return page;
    }
}
