package com.dreamhyuk.dream_order.domain.shop;

import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import jakarta.persistence.*;
import lombok.Getter;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "shop_delivery_types")
@Getter
public class ShopDeliveryType {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_delivery_type_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;


//    private int extraFee; //나중에 추가요금같은 정보를 넣을 수 있음

}
