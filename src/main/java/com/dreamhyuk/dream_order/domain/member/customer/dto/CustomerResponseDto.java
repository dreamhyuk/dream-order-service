package com.dreamhyuk.dream_order.domain.member.customer.dto;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CustomerResponseDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Profile {
        private Long id;
        private String email;
        private String username;

        // 엔티티를 DTO로 안전하게 변환하기 위한 정적 팩토리 메서드
        public static Profile from(Customer customer) {
            return Profile.builder()
                    .id(customer.getId())
                    .email(customer.getEmail())
                    .username(customer.getUsername())
                    .build();
        }
    }
}
