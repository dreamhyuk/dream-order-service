package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.order.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class OrderSummaryResponse {

    private Long orderId;
    private String orderNumber; //가독성 좋은 주문번호 ("A-101")
    private String menuName; //ex."아메리카노 외 2건" - 처럼 가공한 '단일 문자열'
    private int totalPrice;
    private String status;
    private String  createdAt;

    @Builder // 🌟 생성자 레벨 빌더 유지
    private OrderSummaryResponse(Long orderId, String orderNumber, String menuName,
                                int totalPrice, String status, String createdAt) { // 🌟 private을 지워서 패키지 내 접근 허용!
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.menuName = menuName;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static OrderSummaryResponse from(Order order) {

        String generatedOrderNumber = "A-" + order.getId();

        // 🌟 orderDate는 생성 시점에 항상 존재하므로 null 체크 제거!
        String formattedTime = order.getOrderDate().format(DateTimeFormatter.ofPattern("HH:mm"));

        String displayMenuName = order.getOrderItems().isEmpty() ? "메뉴 없음" :
                order.getOrderItems().get(0).getMenu().getMenuName();

        if (order.getOrderItems().size() > 1) {
            displayMenuName += " 외 " + (order.getOrderItems().size() - 1) + "건";
        }

        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(generatedOrderNumber)
                .menuName(displayMenuName)
                .totalPrice(order.getTotalOrderPrice())
                .status(order.getStatus().name()) // 🌟 status도 항상 존재하므로 한 줄로 깔끔하게 매핑!
                .createdAt(formattedTime)
                .build();
    }
}