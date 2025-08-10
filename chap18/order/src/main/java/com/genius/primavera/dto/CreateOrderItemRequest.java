package com.genius.primavera.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CreateOrderItemRequest {
    
    @NotBlank(message = "test IDshould Endpoint")
    private String productId;
    
    @Positive(message = "connection 1should Endpoint connection")
    private int quantity;
    
    @Positive(message = "should 0test connection")
    private BigDecimal unitPrice;
    
    private String productName;
}