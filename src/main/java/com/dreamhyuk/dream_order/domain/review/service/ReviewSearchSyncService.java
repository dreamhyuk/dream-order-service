package com.dreamhyuk.dream_order.domain.review.service;

import com.dreamhyuk.dream_order.domain.review.Review;
import com.dreamhyuk.dream_order.domain.review.ReviewDocument;
import com.dreamhyuk.dream_order.domain.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewSearchSyncService {

    private final ReviewRepository reviewRepository;
    private final ElasticsearchOperations elasticsearchOperations;


    public void syncReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        ReviewDocument doc = ReviewDocument.builder()
                .id(review.getId().toString())
                .shopId(review.getShop().getId().toString())
                .customerId(review.getCustomer().getId())
                .score(review.getScore())
                .content(review.getContent())
                .build();

        elasticsearchOperations.save(doc);
    }
}
