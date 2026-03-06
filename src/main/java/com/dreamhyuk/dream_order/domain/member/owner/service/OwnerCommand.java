package com.dreamhyuk.dream_order.domain.member.owner.service;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import lombok.Builder;
import lombok.Getter;

public class OwnerCommand {

    @Getter
    @Builder
    public static class SignUp {
        private String email;
        private String password;
        private String businessNumber;

        public Owner toEntity(String encodedPassword) {
            return Owner.builder()
                    .email(this.email)
                    .password(encodedPassword)
                    .businessNumber(businessNumber)
                    .role(MemberRole.OWNER)
                    .build();
        }

    }
}
