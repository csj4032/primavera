package com.genius.primavera.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CreateOrderItemRequest {
    
    @NotBlank(message = "상품 ID는 필수입니다")
    private String productId;
    
    @Positive(message = "수량은 1개 이상이어야 합니다")
    private int quantity;
    
    @Positive(message = "단가는 0보다 커야 합니다")
    private BigDecimal unitPrice;
    
    private String productName;
}