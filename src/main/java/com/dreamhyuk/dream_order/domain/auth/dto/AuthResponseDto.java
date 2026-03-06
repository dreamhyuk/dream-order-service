package com.dreamhyuk.dream_order.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Token {
        private String accessToken;
        private String refreshToken;
        private String grantType; // "Bearer"
    }
}
