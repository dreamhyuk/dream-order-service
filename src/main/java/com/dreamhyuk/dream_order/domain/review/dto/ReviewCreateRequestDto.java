package com.dreamhyuk.dream_order.domain.review.dto;

import com.dreamhyuk.dream_order.domain.review.service.ReviewCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequestDto {

    @NotNull(message = "별점을 입력하세요.")
    private Double score;

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    private String content;

    public ReviewCommand.Create toCommand(Long customerId, Long orderId) {
        return ReviewCommand.Create.builder()
                .customerId(customerId)
                .orderId(orderId)
                .score(this.score)
                .content(this.content)
                .build();
    }

}
