package com.dreamhyuk.dream_order.domain.review.controller;

import com.dreamhyuk.dream_order.domain.review.dto.ReviewCreateRequestDto;
import com.dreamhyuk.dream_order.domain.review.service.ReviewCommand;
import com.dreamhyuk.dream_order.domain.review.service.ReviewService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/orders/{orderId}/reviews") //주문(Order) 기반 설계
public class CustomerReviewApiController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Long> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody ReviewCreateRequestDto request) {

        ReviewCommand.Create command = request.toCommand(userDetails.getMemberId(), orderId);

        Long id = reviewService.saveReview(command);

        return ResponseEntity.ok(id);
    }


/*    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getReviews() {

        reviewService.
    }*/

}
