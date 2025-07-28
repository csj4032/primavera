package com.genius.primavera.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OrderCancelledEvent {
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("version")
    private String version;
}