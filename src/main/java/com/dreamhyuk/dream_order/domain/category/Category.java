package com.dreamhyuk.dream_order.domain.category;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    //Category에서 ShopCategory를 참조할 일은 드물다
    //성능상 이점도 없고 관리만 힘들어져서 우선 제거.
//    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
//    private List<ShopCategory> categories = new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String categoryType; //서버용 (ex. CHICKEN, PIZZA, ..)

    private String name; //전시용 (ex. 치킨, 피자 등..)


    public Category(String categoryType, String name) {
        this.categoryType = categoryType;
        this.name = name;
    }
}
