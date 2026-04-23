package com.dreamhyuk.dream_order.domain.review.listener;

import com.dreamhyuk.dream_order.domain.review.service.ReviewSearchSyncService;
import com.dreamhyuk.dream_order.domain.shop.service.ShopSearchSyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventListener {

    private final ShopSearchSyncService shopSyncService;
    private final ReviewSearchSyncService reviewSyncService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "dbserver.mydb.reviews", groupId = "review-sync-group")
    public void handleReviewEvents(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode after = root.path("payload").path("after");

            // 1. 리뷰 도큐먼트 자체 동기화를 위한 reviewId
            Long reviewId = after.path("review_id").asLong();
            // 2. 상점 통계(평점, 리뷰수) 갱신을 위한 shopId
            Long shopId = after.path("shop_id").asLong();

            log.info("리뷰 이벤트 수신: reviewId={}, shopId={}", reviewId, shopId);

            // 상점 도큐먼트 전체 갱신 (메뉴 + 평점 정보 포함)
            // ShopSearchSyncService.sync(shopId) 내부에서 평점/리뷰수를 다시 계산하도록 되어 있다면 베스트
            shopSyncService.sync(shopId);

            // 리뷰 목록 조회를 위한 개별 리뷰 인덱싱
            reviewSyncService.syncReview(reviewId);

        } catch (Exception e) {
            log.error("리뷰 메시지 처리 중 에러 발생", e);
        }
    }
}
