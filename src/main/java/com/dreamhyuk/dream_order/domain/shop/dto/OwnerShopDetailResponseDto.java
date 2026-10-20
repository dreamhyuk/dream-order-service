package com.dreamhyuk.dream_order.domain.shop.dto;

import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopDeliveryType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 사장님 가게 상세 (가게 수정 화면에 현재 값을 채우기 위한 응답)
 * 수정 API(ShopUpdateRequestDto)와 같은 필드 구성이다.
 */
@Getter
@Builder
public class OwnerShopDetailResponseDto {

    private Long shopId;
    private String shopName;

    private String city;
    private String street;
    private String zipcode;

    private List<Long> categoryIds;
    private List<DeliveryType> deliveryTypes;

    public static OwnerShopDetailResponseDto from(Shop shop) {
        return OwnerShopDetailResponseDto.builder()
                .shopId(shop.getId())
                .shopName(shop.getShopName())
                .city(shop.getAddress() != null ? shop.getAddress().getCity() : null)
                .street(shop.getAddress() != null ? shop.getAddress().getStreet() : null)
                .zipcode(shop.getAddress() != null ? shop.getAddress().getZipcode() : null)
                .categoryIds(shop.getShopCategories().stream()
                        .map(shopCategory -> shopCategory.getCategory().getId())
                        .toList())
                .deliveryTypes(shop.getSupportedTypes().stream()
                        .map(ShopDeliveryType::getDeliveryType)
                        .toList())
                .build();
    }
}
