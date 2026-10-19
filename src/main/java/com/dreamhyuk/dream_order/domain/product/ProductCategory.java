package com.dreamhyuk.dream_order.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마트 상품 카테고리. 음식 가게의 Category 와는 별개다.
 */
@Entity
@Table(name = "product_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_category_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int priority;

    //== 생성 메서드 ==//
    public static ProductCategory createProductCategory(String name, int priority) {
        ProductCategory category = new ProductCategory();
        category.name = name;
        category.priority = priority;

        return category;
    }
}
