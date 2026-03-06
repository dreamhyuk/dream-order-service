package com.dreamhyuk.dream_order.domain.order;

public enum OrderStatus {

    PENDING, //접수 대기
    ACCEPTED, //주문 접수
    PREPARING, //조리 중
    DELIVERING, //배달 중 or 픽업 대기 중
    COMP, //완료
    CANCEL //취소

}
