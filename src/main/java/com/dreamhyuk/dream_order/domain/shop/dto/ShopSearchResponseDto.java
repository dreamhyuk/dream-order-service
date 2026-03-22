package com.dreamhyuk.dream_order.domain.shop.dto;

import com.dreamhyuk.dream_order.domain.shop.ShopDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopSearchResponseDto {

    private String id;
    private String name;
    private List<String> categoryNames; // "치킨", "피자" 처럼 이름만 리스트로 전달

    // 정적 팩토리 메서드
    public static ShopSearchResponseDto from(ShopDocument doc) {
        return ShopSearchResponseDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                // ShopDocument의 CategoryInfo 리스트에서 이름(name)만 추출
                .categoryNames(doc.getCategories().stream()
                        .map(ShopDocument.CategoryInfo::getName)
                        .toList())
                .build();
    }
}
