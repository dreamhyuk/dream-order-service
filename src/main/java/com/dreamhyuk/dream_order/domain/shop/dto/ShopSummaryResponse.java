package com.dreamhyuk.dream_order.domain.shop.dto;

import com.dreamhyuk.dream_order.domain.shop.Shop;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ShopSummaryResponse {

    private Long shopId;
    private String shopName;

    //private String shopLogoUrl; //필요시 가볍게 보여줄 로고


    @Builder
    private ShopSummaryResponse(Long shopId, String shopName) {
        this.shopId = shopId;
        this.shopName = shopName;
    }

    public static ShopSummaryResponse from(Shop shop) {
        return ShopSummaryResponse.builder()
                .shopId(shop.getId())
                .shopName(shop.getShopName())
                .build();
    }
}
