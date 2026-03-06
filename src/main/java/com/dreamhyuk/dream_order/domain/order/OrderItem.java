package com.dreamhyuk.dream_order.domain.order;

import com.dreamhyuk.dream_order.domain.menu.Menu;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

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
    public static OrderItem createOrderItem(Menu menu, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.menu = menu;
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
