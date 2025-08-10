package com.genius.primavera.streaming.handler;

import com.genius.primavera.common.dto.ProductDocument;
import com.genius.primavera.streaming.service.ProductSearchService;
import io.debezium.engine.RecordChangeEvent;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Debezium CDC 이벤트 핸들러 단위 테스트")
class DebeziumCdcEventHandlerUnitTest {

    @Mock
    private ProductSearchService productSearchService;

    @InjectMocks
    private DebeziumCdcEventHandler debeziumCdcEventHandler;

    private Schema afterSchema;
    private Schema beforeSchema;

    @BeforeEach
    void setUp() {
        when(productSearchService.indexProduct(any(ProductDocument.class)))
                .thenReturn(Mono.empty());
        when(productSearchService.deleteProduct(any(Long.class)))
                .thenReturn(Mono.empty());

        afterSchema = SchemaBuilder.struct()
                .field("id", Schema.INT64_SCHEMA)
                .field("name", Schema.STRING_SCHEMA)
                .field("description", Schema.STRING_SCHEMA)
                .field("price", Schema.INT32_SCHEMA)
                .field("status", Schema.STRING_SCHEMA)
                .build();

        beforeSchema = afterSchema;
    }

    @Test
    @DisplayName("CREATE 이벤트 처리 테스트")
    void shouldHandleCreateEvent() throws Exception {
        Struct afterStruct = new Struct(afterSchema)
                .put("id", 1L)
                .put("name", "Test Product")
                .put("description", "Test Description")
                .put("price", 50000)
                .put("status", "ACTIVE");

        RecordChangeEvent<SourceRecord> changeEvent = createChangeEvent("c", afterStruct, null);
        
        invokeHandleChangeEvent(changeEvent);

        ArgumentCaptor<ProductDocument> captor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productSearchService).indexProduct(captor.capture());
        
        ProductDocument captured = captor.getValue();
        assertThat(captured.getProductId()).isEqualTo(1L);
        assertThat(captured.getName()).isEqualTo("Test Product");
        assertThat(captured.getDescription()).isEqualTo("Test Description");
        assertThat(captured.getPrice()).isEqualTo(50000);
        assertThat(captured.getStatus()).isEqualTo("ACTIVE");
        assertThat(captured.getPriceRange()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("UPDATE 이벤트 처리 테스트")
    void shouldHandleUpdateEvent() throws Exception {
        Struct afterStruct = new Struct(afterSchema)
                .put("id", 1L)
                .put("name", "Updated Product")
                .put("description", "Updated Description")
                .put("price", 750000)
                .put("status", "ACTIVE");

        RecordChangeEvent<SourceRecord> changeEvent = createChangeEvent("u", afterStruct, null);
        
        invokeHandleChangeEvent(changeEvent);

        ArgumentCaptor<ProductDocument> captor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productSearchService).indexProduct(captor.capture());
        
        ProductDocument captured = captor.getValue();
        assertThat(captured.getPriceRange()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("DELETE 이벤트 처리 테스트")
    void shouldHandleDeleteEvent() throws Exception {
        Struct beforeStruct = new Struct(beforeSchema)
                .put("id", 1L)
                .put("name", "Deleted Product")
                .put("description", "Deleted Description")
                .put("price", 50000)
                .put("status", "ACTIVE");

        RecordChangeEvent<SourceRecord> changeEvent = createChangeEvent("d", null, beforeStruct);
        
        invokeHandleChangeEvent(changeEvent);

        verify(productSearchService).deleteProduct(eq(1L));
        verify(productSearchService, never()).indexProduct(any(ProductDocument.class));
    }

    @Test
    @DisplayName("READ 이벤트 처리 테스트")
    void shouldHandleReadEvent() throws Exception {
        Struct afterStruct = new Struct(afterSchema)
                .put("id", 1L)
                .put("name", "Read Product")
                .put("description", "Read Description")
                .put("price", 1500000)
                .put("status", "ACTIVE");

        RecordChangeEvent<SourceRecord> changeEvent = createChangeEvent("r", afterStruct, null);
        
        invokeHandleChangeEvent(changeEvent);

        ArgumentCaptor<ProductDocument> captor = ArgumentCaptor.forClass(ProductDocument.class);
        verify(productSearchService).indexProduct(captor.capture());
        
        ProductDocument captured = captor.getValue();
        assertThat(captured.getPriceRange()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("가격 범위 결정 로직 테스트")
    void shouldDeterminePriceRange() throws Exception {
        Method method = DebeziumCdcEventHandler.class.getDeclaredMethod("determinePriceRange", Integer.class);
        method.setAccessible(true);

        String lowRange = (String) method.invoke(debeziumCdcEventHandler, 400000);
        String mediumRange = (String) method.invoke(debeziumCdcEventHandler, 750000);
        String highRange = (String) method.invoke(debeziumCdcEventHandler, 1200000);

        assertThat(lowRange).isEqualTo("LOW");
        assertThat(mediumRange).isEqualTo("MEDIUM");
        assertThat(highRange).isEqualTo("HIGH");
    }

    private RecordChangeEvent<SourceRecord> createChangeEvent(String operation, Struct after, Struct before) {
        Schema valueSchema = SchemaBuilder.struct()
                .field("op", Schema.STRING_SCHEMA)
                .field("after", afterSchema)
                .field("before", beforeSchema)
                .build();

        Struct value = new Struct(valueSchema)
                .put("op", operation);
        
        if (after != null) {
            value.put("after", after);
        }
        if (before != null) {
            value.put("before", before);
        }

        SourceRecord sourceRecord = new SourceRecord(
                null, null, "test-topic", null, null, null, valueSchema, value
        );

        return () -> sourceRecord;
    }

    private void invokeHandleChangeEvent(RecordChangeEvent<SourceRecord> changeEvent) throws Exception {
        Method method = DebeziumCdcEventHandler.class.getDeclaredMethod("handleChangeEvent", RecordChangeEvent.class);
        method.setAccessible(true);
        method.invoke(debeziumCdcEventHandler, changeEvent);
    }
}