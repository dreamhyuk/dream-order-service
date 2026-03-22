package com.dreamhyuk.dream_order.domain.shop.dto;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.shop.service.ShopCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ShopCreateRequestDto {

    @NotBlank
    private String shopName;

    private String city;
    private String street;
    private String zipcode;

    // 중간 테이블용 데이터: 단순 리스트로 수신
    @NotEmpty(message = "최소 1개의 카테고리를 선택해야 합니다.")
    private List<Long> categoryIds; // [1, 2] (치킨, 한식 ID 등)

    @NotEmpty(message = "최소 한 가지 이상의 배달 방식을 선택해주세요.")
    private List<DeliveryType> deliveryTypes; // [DREAM_DELIVERY, SHOP_DELIVERY, TAKEOUT]


    public ShopCommand.Create toCommand(Long ownerId) {
        return ShopCommand.Create.builder()
                .ownerId(ownerId)
                .shopName(this.shopName)
                .address(Address.of(this.city, this.street, this.zipcode))
                .categoryIds(this.categoryIds)
                .deliveryTypes(this.deliveryTypes)
                .build();
    }
}
