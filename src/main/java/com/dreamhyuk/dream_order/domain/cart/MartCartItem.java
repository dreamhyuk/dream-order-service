package com.dreamhyuk.dream_order.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MartCartItem {

    private Long productId;
    private String productName;
    private int price;
    private int count;

    public void addCount(int count) {
        this.count += count;
    }
}
