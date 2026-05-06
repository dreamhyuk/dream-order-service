package com.dreamhyuk.dream_order.domain.category;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    @Builder.Default
    private int priority = 0;
//    private String imageUrl;


    public static Category createCategory(String categoryType, String name, int priority) {
        return Category.builder()
                .categoryType(categoryType)
                .name(name)
                .priority(priority)
                .build();
    }
}
