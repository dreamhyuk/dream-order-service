package com.dreamhyuk.dream_order.domain.shop;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<ShopDeliveryType> supportedTypes = new ArrayList<>();

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
}
