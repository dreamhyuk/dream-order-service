package com.dreamhyuk.dream_order.domain.inventory.dto;

import com.dreamhyuk.dream_order.domain.inventory.Inventory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryResponseDto {

    private Long productId;
    private int quantity;
    private int reservedQuantity;

    public static InventoryResponseDto from(Inventory inventory) {
        return InventoryResponseDto.builder()
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .build();
    }
}
