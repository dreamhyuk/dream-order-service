package com.dreamhyuk.dream_order.global.exception;

public class TokenExpiredException extends BusinessException {
    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
