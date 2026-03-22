package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderItem;
import com.dreamhyuk.dream_order.domain.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long orderId;
    private String shopName;
//    private String shopImageUrl;  // 가게 로고나 이미지
    private String orderTitle;     // "후라이드 치킨 외 2건" 같은 대표 명칭
    private Integer totalPrice;    // 총 결제 금액
    private LocalDateTime orderAt; // 주문 일시
    private OrderStatus status;    // 현재 주문 상태 (PENDING, ACCEPTED, COMP 등)

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static OrderResponseDto of(Order order) {
        // 대표 메뉴 명칭 생성 로직 (예: "치즈버거 외 1건")
        String title = createOrderTitle(order);

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .shopName(order.getShop().getShopName())
                .orderTitle(title)
                .totalPrice(order.getTotalOrderPrice())
                .orderAt(order.getOrderDate())
                .status(order.getStatus())
                .build();
    }

    private static String createOrderTitle(Order order) {
        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.isEmpty()) {
            return "주문 내역 없음";
        }

        String firstMenuName = orderItems.get(0).getMenu().getMenuName();
        int otherCount = orderItems.size() - 1;

        return (otherCount > 0)
                ? String.format("%s 외 %d건", firstMenuName, otherCount)
                : firstMenuName;
    }
}
