package com.dreamhyuk.dream_order.domain.cart.controller;

import com.dreamhyuk.dream_order.domain.cart.RedisCart;
import com.dreamhyuk.dream_order.domain.cart.dto.CartRequestDto;
import com.dreamhyuk.dream_order.domain.cart.service.CartService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/carts")
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니에 메뉴 담기
     */
    @PostMapping
    public ResponseEntity<RedisCart> addMenuToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartRequestDto.Add request
    ) {

        cartService.addMenu(
                userDetails.getMemberId(),
                request.getShopId(),
                request.getMenuId(),
                request.getCount()
        );

        RedisCart updatedCart = cartService.getCart(userDetails.getMemberId());
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * 내 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<RedisCart> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RedisCart cart = cartService.getCart(userDetails.getMemberId());
        return ResponseEntity.ok(cart);
    }

    /**
     * 장바구니에서 특정 메뉴 하나 삭제
     */
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<Void> removeMenuFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long menuId
    ) {
        cartService.removeMenu(userDetails.getMemberId(), menuId);
        return ResponseEntity.ok().build();
    }

}
