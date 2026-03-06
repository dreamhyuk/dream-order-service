package com.dreamhyuk.dream_order.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryType {
    // 1. 상수 선언 (생성자 호출)
    DREAM_DELIVERY("드림배달", true),
    SHOP_DELIVERY("가게배달", false),
    TAKEOUT("포장", false);

    // 2. 이 상수가 가질 데이터 변수 (필드)
    private final String description; // "드림배달", "가게배달" 등을 저장
    private final boolean isRiderManaged; // 시스템이 라이더를 배정하는지 여부(T/F)


    /**
     * 배달 주문인지 확인
     */
    public boolean isDelivery() {
        return this == DREAM_DELIVERY || this == SHOP_DELIVERY;
    }

    /**
     * 포장 주문인지 확인하는 로직
     * 서비스 로직에서 (type == DeliveryType.TAKEOUT) 대신
     * type.isTakeout()으로 읽기 쉽게 사용합니다.
     */
    public boolean isTakeout() {
        return this == TAKEOUT;
    }

}
