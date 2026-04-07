package com.dreamhyuk.dream_order.domain.menu;

import com.dreamhyuk.dream_order.domain.shop.Shop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "menu_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_group_id")
    private Long id;

    @OneToMany(mappedBy = "menuGroup")
    private List<Menu> menus = new ArrayList<>();

    @Column(name = "shop_id")
    private Long shopId;

    private String name; //인기메뉴, 메인메뉴, 사이드, 음료..

    //프론트에서 값을 전달 받는다
    private Integer priority; //정렬 순서

    @Builder
    private MenuGroup(String name, Integer priority, Long shopId) {
        this.name = name;
        this.priority = priority;
        this.shopId = shopId;
    }

    public static MenuGroup createMenuGroup(String name, Integer priority, Long shopId) {
        return MenuGroup.builder()
                .name(name)
                .priority(priority)
                .shopId(shopId)
                .build();
    }

    public void update(String name, Integer priority) {
        this.name = name;
        this.priority = priority;
    }

    //== 연관관계 ==//
    public void addMenu(Menu menu) {
        this.menus.add(menu);
        if (menu.getMenuGroup() != this) {
            menu.setMenuGroup(this);
        }
    }
}
