package com.genius.primavera.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InventoryReservedEvent {
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("reservedItems")
    private List<OrderItemEvent> reservedItems;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("version")
    private String version;
}