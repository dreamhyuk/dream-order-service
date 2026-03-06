package com.dreamhyuk.dream_order.domain.order;

import com.dreamhyuk.dream_order.domain.delivery.Delivery;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.shop.Shop;
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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

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
    public void setShop(Shop shop) {
        this.shop = shop;
        shop.getOrders().add(this);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customer.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.connectOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.connectOrder(this);
    }

    //== 생성 메서드 ==//
    public static Order createOrder(Shop shop, Customer customer, Delivery delivery, DeliveryType deliveryType, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setShop(shop);
        order.setCustomer(customer);
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
        if (this.status != OrderStatus.PENDING || this.status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("조리를 시작한 상품은 취소할 수 없습니다.");
        }

        this.status = OrderStatus.CANCEL;
    }
}
