package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.service.OrderCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderRequestDto {

    private Long shopId;
    private List<OrderItemDto> orderItems;
    private Long myAddressId;
    private Address directAddress;
    private DeliveryType deliveryType;

    @Getter
    @NoArgsConstructor
    public static class OrderItemDto {
        private Long menuId;
        private int count;
    }

    //dto를 서비스용 Command로 변환 (사용자 ID는 인증 정보에서 가져옴)
    public OrderCommand.Create toCommand(Long customerId) {
        return OrderCommand.Create.builder()
                .customerId(customerId)
                .shopId(this.shopId)
                .deliveryType(this.deliveryType)
                .myAddressId(this.myAddressId)
                .directAddress(this.directAddress)
                .orderItems(this.orderItems.stream()
                        .map(orderItem -> OrderCommand.Create.OrderItem.builder()
                                .menuId(orderItem.getMenuId())
                                .count(orderItem.getCount())
                                .build())
                        .toList())
                .build();
    }
}
