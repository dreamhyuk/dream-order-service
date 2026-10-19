package com.dreamhyuk.dream_order.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "G001", "잘못된 요청입니다."),

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

    // Shop
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "가게 정보를 찾을 수 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 이메일입니다."),

    // Cart
    CART_EMPTY(HttpStatus.BAD_REQUEST, "C002", "장바구니가 비어 있어 주문을 진행할 수 없습니다."),
    DIFFERENT_SHOP_ERROR(HttpStatus.BAD_REQUEST, "C001", "DIFFERENT_SHOP_ERROR"),
    CART_SHOP_MISMATCH(HttpStatus.BAD_REQUEST, "C002", "장바구니의 가게 정보와 일치하지 않는 메뉴가 포함되어 있습니다."),
    MENU_PRICE_CHANGED(HttpStatus.BAD_REQUEST, "C003", "선택하신 메뉴의 가격이 변동되었습니다. 장바구니를 확인해 주세요."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "C004", "품절된 메뉴가 포함되어 있습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문 정보를 찾을 수 없습니다."),

    // Inventory
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "I001", "재고가 부족한 상품이 포함되어 있습니다."),
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "재고 정보를 찾을 수 없습니다."),

    // Mart Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "상품 정보를 찾을 수 없습니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "T002", "판매 중지된 상품이 포함되어 있습니다."),
    PRODUCT_PRICE_CHANGED(HttpStatus.BAD_REQUEST, "T003", "상품의 가격이 변동되었습니다. 장바구니를 확인해 주세요."),

    // Payment
    PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "P001", "결제가 완료되지 않은 주문입니다."),
    ORDER_NOT_PAYABLE(HttpStatus.BAD_REQUEST, "P002", "결제할 수 없는 주문 상태입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
