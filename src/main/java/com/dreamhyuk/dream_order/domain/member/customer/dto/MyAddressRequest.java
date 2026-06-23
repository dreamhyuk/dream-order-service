package com.dreamhyuk.dream_order.domain.member.customer.dto;

import com.dreamhyuk.dream_order.domain.member.customer.service.AddressCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MyAddressRequest {

    @Getter
    @RequiredArgsConstructor
    public static class Register {
        @NotBlank(message = "주소 별칭은 필수입니다.")
        private final String addressName;

        @NotBlank(message = "시/도는 필수입니다.")
        private final String city;

        @NotBlank(message = "상세 주소는 필수입니다.")
        private final String street;

        @NotBlank(message = "우편번호는 필수입니다.")
        private final String zipcode;


        public AddressCommand.Register toCommand() {
            return new AddressCommand.Register(
                    this.addressName,
                    this.city,
                    this.street,
                    this.zipcode
            );
        }
    }
}
