package com.dreamhyuk.dream_order.domain.product.dto;

import com.dreamhyuk.dream_order.domain.product.Product;
import com.dreamhyuk.dream_order.domain.product.ProductSaleStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDto {

    private Long productId;
    private Long categoryId;
    private String name;
    private int price;
    private ProductSaleStatus saleStatus;
    private boolean soldOut; //가용 재고가 0이면 품절

    public static ProductResponseDto of(Product product, int availableQuantity) {
        return ProductResponseDto.builder()
                .productId(product.getId())
                .categoryId(product.getCategory().getId())
                .name(product.getName())
                .price(product.getPrice())
                .saleStatus(product.getSaleStatus())
                .soldOut(availableQuantity <= 0)
                .build();
    }
}
