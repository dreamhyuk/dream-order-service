package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderCommand {

    @Getter
    @Builder
    public static class Create {
        private final Long customerId;
        private final Long shopId;
        private final Long menuId;
        private final List<OrderItem> orderItems;

        //소비자가 선택할 배달 방식
        private final DeliveryType deliveryType;

        //주소
        private final Long myAddressId; //주소록 선택
        private final Address directAddress; //직접 입력

        //메뉴와 수량을 매칭 (Key: menuId, Value: count)
        public Map<Long, Integer> toMenuCountMap() {
            return orderItems.stream()
                    .collect(Collectors.toMap(
                            OrderItem::getMenuId,
                            OrderItem::getCount
                    ));
        }

        @Getter
        @Builder
        public static class OrderItem {
            private final Long menuId;
            private final int count;
        }
    }

}
