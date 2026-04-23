package com.dreamhyuk.dream_order.domain.review.service;

import lombok.Builder;
import lombok.Getter;

public class ReviewCommand {

    @Getter
    @Builder
    public static class Create {
        private final Long customerId;
//        private final Long shopId;
        private final Long orderId;
        private final Double score;
        private final String content;
    }
}
