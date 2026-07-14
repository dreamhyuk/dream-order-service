package com.dreamhyuk.dream_order.domain.order.controller;

import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.dto.OrderSummaryResponse;
import com.dreamhyuk.dream_order.domain.order.dto.OrderUpdateResponseDto;
import com.dreamhyuk.dream_order.domain.order.service.OrderService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/orders")
@PreAuthorize("hasRole('OWNER')")
public class OwnerOrderApiController {

    private final OrderService orderService;

    /**
     * 사장님 특정 매장의 주문 목록 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getShopOrders(@RequestParam("shopId") Long shopId) {
        List<OrderSummaryResponse> responses = orderService.findOrdersByShopId(shopId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<OrderUpdateResponseDto> acceptOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        //서비스 레이어에 orderId와 함께 현재 로그인한 사장님(Owner)의 ID를 전달하는 게 맞는 거 같음
        Long ownerId = userDetails.getMemberId();

        //예상시간을 30분으로 하드코딩했지만, 나중에 RequestDto 로 예상시간을 받을 수도 있다
        Order updatedOrder = orderService.acceptOrder(orderId, 30);
        OrderUpdateResponseDto response = OrderUpdateResponseDto.of(updatedOrder);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/start-cooking")
    public ResponseEntity<OrderUpdateResponseDto> startCooking(@PathVariable Long orderId) {
        Order updatedOrder = orderService.startCooking(orderId);
        OrderUpdateResponseDto response = OrderUpdateResponseDto.of(updatedOrder);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/complete-cooking")
    public ResponseEntity<OrderUpdateResponseDto> completeCooking(@PathVariable Long orderId) {
        Order updatedOrder = orderService.completeCooking(orderId);
        OrderUpdateResponseDto response = OrderUpdateResponseDto.of(updatedOrder);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/pickup")
    public ResponseEntity<OrderUpdateResponseDto> pickupOrder(@PathVariable Long orderId) {
        Order updatedOrder = orderService.pickupOrder(orderId);
        OrderUpdateResponseDto response = OrderUpdateResponseDto.of(updatedOrder);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<OrderUpdateResponseDto> completeOrder(@PathVariable Long orderId) {
        Order updatedOrder = orderService.completeOrder(orderId);
        OrderUpdateResponseDto response = OrderUpdateResponseDto.of(updatedOrder);

        return ResponseEntity.ok(response);
    }

}
