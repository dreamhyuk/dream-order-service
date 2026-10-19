package com.dreamhyuk.dream_order.domain.menu;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "menu_group_id")
    private MenuGroup menuGroup;

    //성능을 위해 가게 ID를 직접 들고 있음
    @Column(name = "shop_id")
    private Long shopId;

    private String menuName;
    private int price;

    //품절 여부 (음식 메뉴는 수량 재고 없이 사장님이 직접 켜고 끈다)
    @Column(name = "sold_out", nullable = false)
    private boolean soldOut;

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

    //== 비즈니스 로직 ==//
    public void update(String menuName, Integer price) {
        this.menuName = menuName;
        this.price = price;
    }

    public void changeSoldOut(boolean soldOut) {
        this.soldOut = soldOut;
    }
}
