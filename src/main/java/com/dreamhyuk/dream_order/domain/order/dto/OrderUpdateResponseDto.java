package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.delivery.DeliveryStatus;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderUpdateResponseDto {

    private final Long orderId;
    private final OrderStatus orderStatus;
    private final DeliveryStatus deliveryStatus;


    public static OrderUpdateResponseDto of(Order order) {
        return new OrderUpdateResponseDto(
                order.getId(),
                order.getStatus(),
                order.getDelivery().getStatus()
        );
    }

}
