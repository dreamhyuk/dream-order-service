package com.dreamhyuk.dream_order.domain.cart.controller;

import com.dreamhyuk.dream_order.domain.cart.MartCart;
import com.dreamhyuk.dream_order.domain.cart.dto.MartCartRequestDto;
import com.dreamhyuk.dream_order.domain.cart.service.MartCartService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/mart/carts")
public class MartCartController {

    private final MartCartService martCartService;

    /**
     * 마트 장바구니에 상품 담기
     */
    @PostMapping
    public ResponseEntity<MartCart> addProductToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MartCartRequestDto.Add request
    ) {
        martCartService.addProduct(userDetails.getMemberId(), request.getProductId(), request.getCount());

        return ResponseEntity.ok(martCartService.getCart(userDetails.getMemberId()));
    }

    /**
     * 내 마트 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<MartCart> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(martCartService.getCart(userDetails.getMemberId()));
    }

    /**
     * 마트 장바구니에서 상품 하나 삭제
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> removeProductFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId
    ) {
        martCartService.removeProduct(userDetails.getMemberId(), productId);

        return ResponseEntity.ok().build();
    }
}
