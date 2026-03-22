package com.dreamhyuk.dream_order.domain.shop;

import com.dreamhyuk.dream_order.domain.category.Category;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopDeliveryType> supportedTypes = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopCategory> shopCategories = new ArrayList<>();

    @Embedded
    private Address address;

    private String shopName;

    //== 비즈니스 로직 ==//
    /**
     * 가게가 지원하는 배달 방식인지 검증
     */
    public void validateSupport(DeliveryType deliveryType) {
        boolean isSupported = this.supportedTypes.stream()
                .anyMatch(s -> s.getDeliveryType() == deliveryType);

        if (!isSupported) {
            throw new IllegalArgumentException(String.format("[%s] 가게는 [%s] 방식을 지원하지 않습니다.",
                    shopName, deliveryType.getDescription()));
        }
    }

    //== 생성 메서드 ==//
    public static Shop createShop(Owner owner, List<DeliveryType> deliveryTypes,
                                  List<Category> categories, Address address, String name) {
        Shop shop = new Shop();
        shop.setOwner(owner);
        shop.shopName = name;
        shop.address = address;

        shop.addCategory(categories);
        shop.addDeliveryTypes(deliveryTypes);

        return shop;
    }

    //== 연관관계 편의 메서드 ==//
    public void setOwner(Owner owner) {
        this.owner = owner;
        owner.getShops().add(this); // Owner 엔티티에 getShops() 리스트가 있다고 가정
    }

    //== 비즈니스 로직 ==//
    public void addCategory(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("최소 1개의 카테고리를 선택해야 합니다.");
        }
        if (categories.size() > 2) {
            throw new IllegalArgumentException("카테고리는 최대 2개만 선택할 수 있습니다.");
        }

        this.shopCategories.clear();

        for (Category category: categories) {
            ShopCategory shopCategory = ShopCategory.createShopCategory(category);
            this.shopCategories.add(shopCategory);
            shopCategory.setShop(this);
        }
    }

    public void addDeliveryTypes(List<DeliveryType> deliveryTypes) {
        if (deliveryTypes == null || deliveryTypes.isEmpty()) {
            throw new IllegalArgumentException("최소 한 가지 이상의 배달 방식을 지원해야 합니다.");
        }

        this.supportedTypes.clear();
        for (DeliveryType type : deliveryTypes) {
            ShopDeliveryType shopDeliveryType = ShopDeliveryType.createShopDeliveryType(type);
            this.supportedTypes.add(shopDeliveryType);
            shopDeliveryType.setShop(this);
        }
    }
}
