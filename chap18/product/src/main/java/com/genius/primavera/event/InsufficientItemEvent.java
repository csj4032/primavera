package com.genius.primavera.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsufficientItemEvent {
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("requestedQuantity")
    private int requestedQuantity;
    
    @JsonProperty("availableQuantity")
    private int availableQuantity;
}