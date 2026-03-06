package com.dreamhyuk.dream_order.domain.order;

import com.dreamhyuk.dream_order.domain.order.dto.OrderRequestDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderUpdateResponseDto;
import com.dreamhyuk.dream_order.domain.order.service.OrderCommand;
import com.dreamhyuk.dream_order.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderRequestDto orderRequest) {
        // 로그인 아직 미구현
        // 1. 실제로는 인증 세션에서 customerId를 가져와야 한다. (임시로 1L 세팅)
        Long currentCustomerId = 1L;

        // 2. DTO -> Command 변환
        OrderCommand.Create command = orderRequest.toCommand(currentCustomerId);

        // 3. 서비스 호출
        Long orderId = orderService.order(command);

        // 4. 생성된 주문 ID 반환
        return ResponseEntity.ok(orderId);
    }

    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<OrderUpdateResponseDto> acceptOrder(@PathVariable Long orderId) {
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
