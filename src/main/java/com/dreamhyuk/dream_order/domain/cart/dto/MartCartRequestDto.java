package com.dreamhyuk.dream_order.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MartCartRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Add {
        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;

        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        private int count;
    }
}
