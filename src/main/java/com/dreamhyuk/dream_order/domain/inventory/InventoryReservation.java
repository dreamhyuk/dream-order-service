package com.dreamhyuk.dream_order.domain.inventory;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주문별 재고 선점 내역.
 * Order, Product 를 객체로 참조하지 않고 ID 만 가진다.
 */
@Entity
@Table(name = "inventory_reservations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryReservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    //== 생성 메서드 ==//
    public static InventoryReservation createReservation(Long orderId, Long productId, int quantity) {
        InventoryReservation reservation = new InventoryReservation();
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = LocalDateTime.now();

        return reservation;
    }

    //== 비즈니스 로직 ==//
    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }

    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }

    /** 선점 확정 */
    public void confirm() {
        if (this.status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("선점 상태인 내역만 확정할 수 있습니다.");
        }

        this.status = ReservationStatus.CONFIRMED;
    }

    /** 선점 해제 */
    public void release() {
        if (this.status == ReservationStatus.RELEASED) {
            throw new IllegalStateException("이미 해제된 내역입니다.");
        }

        this.status = ReservationStatus.RELEASED;
    }
}
