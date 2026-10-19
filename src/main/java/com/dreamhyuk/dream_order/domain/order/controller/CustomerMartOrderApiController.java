package com.dreamhyuk.dream_order.domain.order.controller;

import com.dreamhyuk.dream_order.domain.order.dto.OrderRequest;
import com.dreamhyuk.dream_order.domain.order.service.MartOrderService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 마트 주문 생성.
 * 결제, 취소, 조회는 음식 주문과 같은 API(/api/customers/orders/...)를 쓴다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/mart/orders")
public class CustomerMartOrderApiController {

    private final MartOrderService martOrderService;

    @PostMapping
    public ResponseEntity<Long> createOrderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrderRequest.CreateMart request
    ) {
        Long orderId = martOrderService.saveOrderFromCart(userDetails.getMemberId(), request.toCommand());

        return ResponseEntity.ok(orderId);
    }
}
