package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class OrderDetailResponseDto {

    private Long orderId;
    private String shopName;
    private OrderStatus status;
    private LocalDateTime orderDate;

    // 상세 메뉴 내역 (List)
    private List<OrderItemResponseDto> orderItems;

    private int totalPrice;
    private String deliveryAddress; // 배달 주소
//    private DeliveryType deliveryType;

//    private String requestMessage;  // 요청 사항

    // 정적 팩토리 메서드
    public static OrderDetailResponseDto of(Order order) {
        return OrderDetailResponseDto.builder()
                .orderId(order.getId())
                .shopName(order.getShop().getShopName())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemResponseDto::of)
                        .collect(Collectors.toList()))
                .totalPrice(order.getTotalOrderPrice())
                .deliveryAddress(order.getDelivery().getFullAddress())
//                .requestMessage(order.getRequestMessage())
                .build();
    }


}
