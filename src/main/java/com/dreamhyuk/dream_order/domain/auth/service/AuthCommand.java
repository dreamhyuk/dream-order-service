package com.dreamhyuk.dream_order.domain.auth.service;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import lombok.Builder;
import lombok.Getter;

public class AuthCommand {

    @Getter
    @Builder
    public static class Login {
        private String email;
        private String password;
        private MemberRole role;
    }
}
