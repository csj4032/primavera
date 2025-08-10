package com.genius.primavera.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "test IDshould Endpoint")
    private String customerId;
    
    @NotEmpty(message = "test connection test 1should Endpoint connection")
    @Valid
    private List<CreateOrderItemRequest> items;
}