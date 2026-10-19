package com.dreamhyuk.dream_order.domain.inventory;

public enum ReservationStatus {

    RESERVED, //선점 (결제 대기)
    CONFIRMED, //확정 (결제 완료)
    RELEASED //해제 (주문 취소)

}
