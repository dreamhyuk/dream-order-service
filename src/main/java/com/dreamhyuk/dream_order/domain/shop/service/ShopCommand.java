package com.dreamhyuk.dream_order.domain.shop.service;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ShopCommand {

    @Getter
    @Builder
    public static class Create {
        private final Long ownerId;
        private final String shopName;
        private final Address address;
        private final List<Long> categoryIds;
        private final List<DeliveryType> deliveryTypes;
    }
}
