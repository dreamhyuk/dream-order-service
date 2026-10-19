package com.dreamhyuk.dream_order.domain.order;

import com.dreamhyuk.dream_order.domain.delivery.Delivery;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "orders")
@Getter
@RequiredArgsConstructor
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    //주문은 음식/마트 공통 도메인이라 가게, 회원을 객체로 참조하지 않고 ID만 가진다
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    //판매처 ID (음식 주문이면 가게 ID, 마트 주문이면 센터 ID)
    @Column(name = "seller_id")
    private Long sellerId;

    //주문 시점의 판매처 이름 (나중에 가게 이름이 바뀌어도 주문 내역은 그대로)
    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "customer_id")
    private Long customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;


    public boolean isDreamDelivery() {
        return this.deliveryType == DeliveryType.DREAM_DELIVERY;
    }

    //== 연관관계 메서드 ==//
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.connectOrder(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.connectOrder(this);
    }

    //== 생성 메서드 ==//
    public static Order createOrder(OrderType orderType, Long sellerId, String sellerName, Long customerId,
                                    Delivery delivery, DeliveryType deliveryType, List<OrderItem> orderItems) {
        Order order = new Order();
        order.orderType = orderType;
        order.sellerId = sellerId;
        order.sellerName = sellerName;
        order.customerId = customerId;
        order.setDelivery(delivery);

        for (OrderItem orderItem: orderItems) {
            order.addOrderItem(orderItem);
        }

        order.deliveryType = deliveryType;
        order.status = OrderStatus.PENDING;
        order.orderDate = LocalDateTime.now();

        return order;
    }

    //== 비즈니스 로직 ==//
    /** 주문 접수 */
    public void accept() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 접수할 수 있습니다.");
        }
        //주문상태 변경
        this.status = OrderStatus.ACCEPTED;

        //결합도를 낮추기 위해 deliveryStatus 변경 로직은 Service에서 호출하도록 하겠다.
    }

    /** 조리 시작 */
    public void startCooking() {
        if (this.status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("접수된 주문이 아닙니다.");
        }

        this.status = OrderStatus.PREPARING;
    }

    /** 배송 및 픽업 대기 중 */
    public void startDelivering() {
        if (this.status != OrderStatus.PREPARING) {
            throw new IllegalStateException("상품이 준비되지 않았습니다.");
        }

        this.status = OrderStatus.DELIVERING;
    }

    /** 최종 주문 완료 */
    public void complete() {
        if (this.status != OrderStatus.DELIVERING) {
            throw new IllegalStateException("배송 중인 주문만 완료 처리할 수 있습니다.");
        }

        this.status = OrderStatus.COMP;
    }

    /** 주문 취소 */
    public void cancel() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("조리를 시작한 상품은 취소할 수 없습니다.");
        }

        this.status = OrderStatus.CANCEL;
    }

    /**
     * 주문 전체 금액 계산
     * Order에 속한 OrderItem 들의 가격을 더한다
     */
    public int getTotalOrderPrice() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}
