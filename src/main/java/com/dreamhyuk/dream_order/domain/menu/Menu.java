package com.dreamhyuk.dream_order.domain.menu;

import com.dreamhyuk.dream_order.domain.order.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @OneToMany(mappedBy = "menu")
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "menu_group_id")
    private MenuGroup menuGroup;

    //성능을 위해 가게 ID를 직접 들고 있음
    @Column(name = "shop_id")
    private Long shopId;

    private String menuName;
    private int price;

    @Builder
    private Menu(String menuName, int price, Long shopId, MenuGroup menuGroup) {
        this.menuName = menuName;
        this.price = price;
        this.shopId = shopId;
        this.menuGroup = menuGroup;
    }

    //== 생성 메서드 ==//
    public static Menu createMenu(String menuName, int price, Long shopId, MenuGroup menuGroup) {

        return Menu.builder()
                .menuName(menuName)
                .price(price)
                .shopId(shopId)
                .menuGroup(menuGroup)
                .build();
    }

    //== 연관관계 =//
    protected void setMenuGroup(MenuGroup menuGroup) {
        this.menuGroup = menuGroup;
    }
}
