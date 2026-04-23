package com.dreamhyuk.dream_order.domain.shop;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Document(indexName = "shops")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setting(settingPath = "/elasticsearch/settings.json")
public class ShopDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Object)
    private List<CategoryInfo> categories;

    /**
     * 만약 평균 별점 4.8의 100회 이상 참여된 가게 A와
     * 평균 별점 5.0의 1회 참여된 가게 B를 비교해서
     * B가 A보다 상단에 위치하는 것을 방지하려고 '별점'과 '리뷰 수'를 함께 관리한다.
     * --------------------------
     * 보통 **Function Score Query**를 사용하여 averageRating과 reviewCount를 조합한 가중치 점수를 만들어 정렬
     */
    @Field(type = FieldType.Double)
    private Double averageRating; // 평균 별점 (예: 4.8)

    @Field(type = FieldType.Integer)
    private Integer reviewCount;  // 리뷰 수


    // --- 메뉴 그룹 ---
    @Field(type = FieldType.Nested) // 데이터 간의 관계 유지를 위해 Nested 권장
    private List<MenuGroupInfo> menuGroups;

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        @Field(type = FieldType.Keyword) // 필터링 용도는 Keyword
        private String type; //검색 시 "categories.type"으로 접근
        private String name;
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuGroupInfo {
        private String groupName;

        @Field(type = FieldType.Nested)
        private List<MenuInfo> menus;
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuInfo {
        @Field(type = FieldType.Text, analyzer = "nori") // 메뉴명으로 검색이 이뤄질 수 있다.
        private String menuName;
    }

    public static ShopDocument from(Shop shop, List<MenuGroup> menuGroups) {
        return ShopDocument.builder()
                .id(shop.getId().toString())
                .name(shop.getShopName())
                .averageRating(shop.getAverageRating())
                .reviewCount(shop.getReviewCount())
                // Shop 엔티티 내부의 ShopCategory 리스트를 순회하며 CategoryInfo로 추출
                .categories(shop.getShopCategories().stream()
                        .map(sc -> new CategoryInfo(
                                sc.getCategory().getCategoryType(), // 고유 코드 (CHICKEN 등)
                                sc.getCategory().getName()         // 출력될 이름 (치킨 등)
                        ))
                        .toList())
                .menuGroups(menuGroups.stream()
                        .map(group -> new MenuGroupInfo(
                                group.getName(),
                                group.getMenus().stream()
                                        .map(menu -> new MenuInfo(menu.getMenuName()))
                                        .toList()
                        )).toList())
                .build();
    }
}