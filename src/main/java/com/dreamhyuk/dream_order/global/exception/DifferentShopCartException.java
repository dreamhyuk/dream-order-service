package com.dreamhyuk.dream_order.global.exception;

public class DifferentShopCartException extends BusinessException {
    public DifferentShopCartException() {
        super(ErrorCode.DIFFERENT_SHOP_ERROR);
    }
}
