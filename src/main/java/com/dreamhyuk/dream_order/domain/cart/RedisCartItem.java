package com.dreamhyuk.dream_order.domain.cart;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedisCartItem {
    private Long menuId;
    private String menuName;
    private int price;
    private int count;

    public void addCount(int count) {
        this.count += count;
    }
}
