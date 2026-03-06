package com.dreamhyuk.dream_order.domain.delivery;

public enum DeliveryStatus {

    NONE, //해당 업음
    PENDING, //배차 대기(라이더 찾는 중)
    READY_FOR_PICKUP, //조리 완료 (라이더/포장 고객 기다림)
    DELIVERING, //배달 중
    DELIVERED, //배송 완료
}
