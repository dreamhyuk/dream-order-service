package com.dreamhyuk.dream_order.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    //주문 대상 ID (음식 주문이면 메뉴 ID, 마트 주문이면 상품 ID). 어느 쪽인지는 Order.orderType 으로 구분한다
    @Column(name = "item_id")
    private Long itemId;

    //주문 시점의 이름 (나중에 메뉴/상품 이름이 바뀌어도 주문 내역은 그대로)
    @Column(name = "item_name")
    private String itemName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;
    private int count;

    //== 연관관계 메서드 용 ==//
    protected void connectOrder(Order order) {
        this.order = order;
    }

    //== 생성 메서드 ==//
    public static OrderItem createOrderItem(Long itemId, String itemName, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.itemId = itemId;
        orderItem.itemName = itemName;
        orderItem.orderPrice = orderPrice;
        orderItem.count = count;

        return orderItem;
    }

    //== 조회 로직 ==//
    /** 주문상품 전체 가격 조회 */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }

}
