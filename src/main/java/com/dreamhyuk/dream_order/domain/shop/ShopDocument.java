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
@Setting(settingPath = "elasticsearch/settings.json")
public class ShopDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Object)
    private List<CategoryInfo> categories;

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
        private Integer price;
    }

    public static ShopDocument from(Shop shop, List<MenuGroup> menuGroups) {
        return ShopDocument.builder()
                .id(shop.getId().toString())
                .name(shop.getShopName())
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
                                        .map(menu -> new MenuInfo(menu.getMenuName(), menu.getPrice()))
                                        .toList()
                        )).toList())
                .build();
    }
}