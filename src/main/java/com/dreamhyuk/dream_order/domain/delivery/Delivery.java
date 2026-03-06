package com.dreamhyuk.dream_order.domain.delivery;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import static jakarta.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Delivery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

//    @Enumerated(EnumType.STRING)
//    private DeliveryType type;

    //== 연관관계 용 ==//
    /**
     * 주의: 이 메서드는 Order 엔티티와의 연관관계 편의 메서드 전용
     * 외부 Service에서 직접 호출하지 마세요.
     */
    public void connectOrder(Order order) {
        this.order = order;
    }

    //== 생성 메서드 ==//
    /** 주문 시점에 배송 정보를 생성하려는 의도 */
    public static Delivery createDelivery(Address address, DeliveryType type) {
        // 포장일 때는 주소가 필수값이 아닐 수도 있다
        if (!type.isTakeout()) {
            Assert.notNull(address, "배달 주문 시 주소는 필수입니다.");
        }

        return Delivery.builder()
                .address(address)
                .status(DeliveryStatus.NONE) // 초기 상태 강제
//                .status(initialStatus)
//                .type(type) // 엔티티에 타입 저장
                .build();
    }

    //== 비즈니스 로직 ==//
    /** 배차/준비 대기 */
    public void pending() {
        this.status = DeliveryStatus.PENDING;
    }

    /** 조리 완료 (누군가 가져갈 준비가 됨) */
    public void ready() {
        if (this.status != DeliveryStatus.PENDING) {
            throw new IllegalStateException("대기 상태에서만 준비 완료가 가능합니다.");
        }
        this.status = DeliveryStatus.READY_FOR_PICKUP;
    }

    /** 라이더/고객 픽업 (이동 시작) */
    public void delivering() {
        if (this.status != DeliveryStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("준비 완료된 상품만 픽업할 수 있습니다.");
        }
        this.status = DeliveryStatus.DELIVERING;
    }

    /** 전달 완료 */
    public void complete() {
        if (this.status != DeliveryStatus.DELIVERING) {
            throw new IllegalStateException("배송 중인 상태에서만 완료 처리가 가능합니다.");
        }
        this.status = DeliveryStatus.DELIVERED;
    }

}
