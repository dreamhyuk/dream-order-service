package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.order.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OrderItemResponseDto {

    private String menuName;    // 메뉴 이름
    private int orderPrice;     // 주문 당시 단가
    private int count;          // 수량
    private int totalPrice; // 단가 * 수량

    public static OrderItemResponseDto of(OrderItem orderItem) {
        return OrderItemResponseDto.builder()
                .menuName(orderItem.getMenu().getMenuName())
                .orderPrice(orderItem.getOrderPrice())
                .count(orderItem.getCount())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}
