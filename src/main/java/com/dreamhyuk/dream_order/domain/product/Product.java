package com.dreamhyuk.dream_order.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마트 상품.
 * 수량 재고는 Inventory 가 productId 로 따로 관리하고, 여기에는 판매 상태만 둔다.
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_category_id")
    private ProductCategory category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false)
    private ProductSaleStatus saleStatus;

    //== 생성 메서드 ==//
    public static Product createProduct(ProductCategory category, String name, int price) {
        Product product = new Product();
        product.category = category;
        product.name = name;
        product.price = price;
        product.saleStatus = ProductSaleStatus.ON_SALE;

        return product;
    }

    //== 비즈니스 로직 ==//
    public boolean isOnSale() {
        return this.saleStatus == ProductSaleStatus.ON_SALE;
    }

    public void update(String name, int price) {
        this.name = name;
        this.price = price;
    }

    /** 판매 중지 */
    public void stopSale() {
        this.saleStatus = ProductSaleStatus.STOPPED;
    }

    /** 판매 재개 */
    public void resumeSale() {
        this.saleStatus = ProductSaleStatus.ON_SALE;
    }
}
