package com.dreamhyuk.dream_order.domain.shop.dto;

import com.dreamhyuk.dream_order.domain.shop.service.ShopSearchCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ShopSearchRequestDto {

    //검색 핵심 조건
    private String keyword;      // 검색어 (키워드 검색 시 사용)
    private String categoryType;     // 카테고리 코드 (카테고리 검색 시 사용)

    //위치 정보
//    @NotNull
//    private Double latitude;     // 위도
//    @NotNull
//    private Double longitude;    // 경도

    //필터 및 정렬 (ex. 최소주문금액 필터, 영업중인 가게만 보기 필터 등..)
//    private String sortBy = "distance"; //기본값으로 거리순 (정렬: distance, rating, review_count 등..)
//    private int minOrderPrice; //최소주문금액 필터
//    private boolean isOpenOnly = true; //영업중인 가게만 보기

    //페이징
//    private int page = 0;
//    private int size = 10;


    public ShopSearchCommand toCommand() {
        return ShopSearchCommand.of(this.keyword, this.categoryType);
    }

}
