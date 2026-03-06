package com.dreamhyuk.dream_order.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Auth
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A001", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "A003", "로그인에 실패하였습니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "A004", "유효하지 않은 로그인 타입입니다."),
    // 토큰 재발급 관련 (401 Unauthorized)
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "리프레시 토큰이 유효하지 않거나 변조되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A006", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "A007", "토큰의 타입이 일치하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A008", "로그아웃되었거나 유효하지 않은 세션입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
