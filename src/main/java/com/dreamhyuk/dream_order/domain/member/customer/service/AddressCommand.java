package com.dreamhyuk.dream_order.domain.member.customer.service;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AddressCommand {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Register {

        private String addressName; // 예: "우리집", "회사", "할머니댁"
        private String city;
        private String street;
        private String zipcode;

        /**
         * 서비스 레이어에서 엔티티로 편하게 변환하기 위한 빌더 메서드
         */
        public MyAddress toEntity(Customer customer) {
            return MyAddress.builder()
                    .addressName(this.addressName)
                    .customer(customer)
                    .address(new Address(this.city, this.street, this.zipcode)) // 값 객체 생성
                    .build();
        }
    }
}