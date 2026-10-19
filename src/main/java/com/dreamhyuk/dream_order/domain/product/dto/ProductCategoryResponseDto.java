package com.dreamhyuk.dream_order.domain.product.dto;

import com.dreamhyuk.dream_order.domain.product.ProductCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductCategoryResponseDto {

    private Long categoryId;
    private String name;

    public static ProductCategoryResponseDto from(ProductCategory category) {
        return ProductCategoryResponseDto.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .build();
    }
}
