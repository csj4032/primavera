package com.genius.primavera.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CreateOrderItemRequest {
    
    @NotBlank(message = "translated_text_2 IDtranslated_text_1 translated_text_5")
    private String productId;
    
    @Positive(message = "translated_text_3 1translated_text_1 translated_text_5 translated_text_3")
    private int quantity;
    
    @Positive(message = "translated_text_1 0translated_text_2 translated_text_2 translated_text_3")
    private BigDecimal unitPrice;
    
    private String productName;
}