package com.dreamhyuk.dream_order.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CartRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Add {

        @NotNull(message = "가게 ID는 필수입니다.")
        private Long shopId;

        @NotNull(message = "메뉴 ID는 필수입니다.")
        private Long menuId;

        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        private int count;
    }

    /**
     * 장바구니 화면 내에서 +, - 버튼을 눌러 수량을 조절할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCount {

        @NotNull(message = "메뉴 ID는 필수입니다.")
        private Long menuId;

        @Min(value = 1, message = "변경할 수량은 최소 1개 이상이어야 합니다.")
        private int count; // 증가량이 아니라 '최종 변경될 수량'을 받으면 로직이 깔끔해짐
    }
}
