package com.dreamhyuk.dream_order.domain.order.controller;

import com.dreamhyuk.dream_order.domain.order.dto.OrderDetailResponseDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderRequest;
import com.dreamhyuk.dream_order.domain.order.dto.OrderRequestDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderResponseDto;
import com.dreamhyuk.dream_order.domain.order.service.OrderCommand;
import com.dreamhyuk.dream_order.domain.order.service.OrderService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderApiController {

    private final OrderService orderService;


    @PostMapping
    public ResponseEntity<Long> createOrderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrderRequest.Create request
    ) {
        // 🌟 인증된 유저 ID와 프론트의 요청 데이터를 각각의 파라미터로 명확하게 꽂아줍니다.
        // request.toCommand() 내부에는 customerId를 조립하는 지저분한 코드가 들어가지 않습니다.
        Long orderId = orderService.saveOrderFromCart(userDetails.getMemberId(), request.toCommand());

        return ResponseEntity.ok(orderId);
    }

/*    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestBody OrderRequestDto orderRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long customerId = userDetails.getMemberId();

        // 2. DTO -> Command 변환
        OrderCommand.Create command = orderRequest.toCommand(customerId);

        // 3. 서비스 호출
        Long orderId = orderService.saveOrder(command);

        // 4. 생성된 주문 ID 반환
        return ResponseEntity.ok(orderId);
    }*/

    /**
     * 주문 조회
     */
    //전체 주문 조회
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<OrderResponseDto> responses = orderService.findAllOrders(userDetails.getMemberId(), pageable);

        return ResponseEntity.ok(responses);
    }

    //상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetail(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {

        //내 주문이 맞는지 검증 로직 필요

        OrderDetailResponseDto response = orderService.findOrderDetail(orderId, userDetails.getMemberId());

        return ResponseEntity.ok(response);
    }

    //진행 중인 주문
    @GetMapping("/active")
    public ResponseEntity<List<OrderResponseDto>> getActiveOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        //취소나 완료되지 않은 주문을 조회
        List<OrderResponseDto> responses = orderService.findActiveOrders(userDetails.getMemberId());

        return ResponseEntity.ok(responses);
    }
}
