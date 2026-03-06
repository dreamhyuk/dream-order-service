package com.dreamhyuk.dream_order.domain.member.customer.service;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import lombok.Builder;
import lombok.Getter;

public class CustomerCommand {

    @Getter
    @Builder
    public static class SingUp {
        private String email;
        private String password;
        private String username;

        public Customer toEntity(String encodedPassword) {
            return Customer.builder()
                    .email(this.email)
                    .password(encodedPassword)
                    .username(this.username)
                    .role(MemberRole.CUSTOMER)
                    .build();
        }
    }

    //나중에 프로필 수정이 필요하면 추가
/*
    @Getter
    @Builder
    public static class UpdateProfile {
        private String nickname;
    }
*/
}
