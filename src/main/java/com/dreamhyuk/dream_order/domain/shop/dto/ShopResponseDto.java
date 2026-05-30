package com.dreamhyuk.dream_order.domain.shop.dto;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponseDto {

    private String shopName;
    private Address address;
    private Double averageRating;
    private Integer reviewCount;

    public static ShopResponseDto from(Shop shop) {
        return ShopResponseDto.builder()
                .shopName(shop.getShopName())
                .address(shop.getAddress())
                .averageRating(shop.getAverageRating())
                .reviewCount(shop.getReviewCount())
                .build();
    }

}
