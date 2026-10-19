package com.dreamhyuk.dream_order.domain.order.dto;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.service.OrderCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OrderRequest {

    @Getter
    @NoArgsConstructor
    public static class Create {

        //배달 방식 및 배송지 정보만 수신
        private DeliveryType deliveryType;
        private Long myAddressId;
        private Address directAddress;

        /**
         * 이제 customerId 없이, 프론트가 준 순수 데이터만 커맨드로 바꿈
         */
        public OrderCommand.CreateFromCart toCommand() {
            return OrderCommand.CreateFromCart.builder()
                    .deliveryType(this.deliveryType)
                    .myAddressId(this.myAddressId)
                    .directAddress(this.directAddress)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CreateMart {

        //마트 주문은 항상 배달이므로 배송지 정보만 수신
        private Long myAddressId;
        private Address directAddress;

        public OrderCommand.CreateMartFromCart toCommand() {
            return OrderCommand.CreateMartFromCart.builder()
                    .myAddressId(this.myAddressId)
                    .directAddress(this.directAddress)
                    .build();
        }
    }
}
