package com.dreamhyuk.dream_order.domain.shop;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Document(indexName = "shops")
@Getter
@Setting(settingPath = "elasticsearch/settings.json")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Object)
    private List<CategoryInfo> categories;

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        @Field(type = FieldType.Keyword) // 필터링 용도는 Keyword
        private String type; //검색 시 "categories.type"으로 접근

        private String name;
    }


    public static ShopDocument from(Shop shop) {
        return ShopDocument.builder()
                .id(shop.getId().toString())
                .name(shop.getShopName())
                // Shop 엔티티 내부의 ShopCategory 리스트를 순회하며 CategoryInfo로 추출
                .categories(shop.getShopCategories().stream()
                        .map(sc -> new CategoryInfo(
                                sc.getCategory().getCategoryType(), // 고유 코드 (CHICKEN 등)
                                sc.getCategory().getName()         // 전시 이름 (치킨 등)
                        ))
                        .toList())
                .build();
    }
}
