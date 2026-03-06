package com.dreamhyuk.dream_order.domain.menu;

import com.dreamhyuk.dream_order.domain.order.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    private String menuName;
    private int price;
}
