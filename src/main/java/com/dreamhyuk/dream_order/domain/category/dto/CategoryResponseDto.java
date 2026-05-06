package com.dreamhyuk.dream_order.domain.category.dto;

import com.dreamhyuk.dream_order.domain.category.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponseDto {

    //카테고리 클릭 시 상세 리스트 조회를 위해
    private Long id;

    private String categoryName;

//    private String imageUrl;

    private Integer priority;

    public static CategoryResponseDto from(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .categoryName(category.getName())
//                .imageUrl(category.getImageUrl())
                .priority(category.getPriority())
                .build();
    }
}
