package com.dreamhyuk.dream_order.domain.shop;

import com.dreamhyuk.dream_order.domain.category.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "shop_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shop_id", "category_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_category_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    //== 생성 메서드 ==//
    public static ShopCategory createShopCategory(Category category) {
        ShopCategory shopCategory = new ShopCategory();
        shopCategory.category = category;

        return shopCategory;
    }


    //== 완관관계 편의 메서드용==//
    public void setShop(Shop shop) {
        this.shop = shop;
    }
}