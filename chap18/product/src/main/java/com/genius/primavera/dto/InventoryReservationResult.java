package com.genius.primavera.dto;

import com.genius.primavera.event.InsufficientItemEvent;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InventoryReservationResult {
    
    private boolean success;
    private String reason;
    private List<InsufficientItemEvent> insufficientItems;
    
    public static InventoryReservationResult success() {
        return InventoryReservationResult.builder()
                .success(true)
                .build();
    }
    
    public static InventoryReservationResult failure(String reason, List<InsufficientItemEvent> insufficientItems) {
        return InventoryReservationResult.builder()
                .success(false)
                .reason(reason)
                .insufficientItems(insufficientItems)
                .build();
    }
}