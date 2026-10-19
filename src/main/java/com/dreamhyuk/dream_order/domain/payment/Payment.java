package com.dreamhyuk.dream_order.domain.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문 한 건의 결제. 외부 PG 연동 없이 결제 상태만 기록하는 가짜 결제다.
 * 추후 별도 서비스로 분리할 수 있도록 Order 를 객체로 참조하지 않고 orderId 만 가진다.
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    //== 생성 메서드 ==//
    public static Payment createPaid(Long orderId, int amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.PAID;
        payment.paidAt = LocalDateTime.now();

        return payment;
    }

    //== 비즈니스 로직 ==//
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    /** 결제 취소 */
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}
