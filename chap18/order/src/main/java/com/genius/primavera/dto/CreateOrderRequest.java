package com.genius.primavera.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "고객 ID는 필수입니다")
    private String customerId;
    
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다")
    @Valid
    private List<CreateOrderItemRequest> items;
}