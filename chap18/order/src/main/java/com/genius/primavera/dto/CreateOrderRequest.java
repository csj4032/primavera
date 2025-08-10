package com.genius.primavera.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "translated_text_2 IDtranslated_text_1 translated_text_5")
    private String customerId;
    
    @NotEmpty(message = "translated_text_2 translated_text_3 translated_text_2 1translated_text_1 translated_text_5 translated_text_3")
    @Valid
    private List<CreateOrderItemRequest> items;
}