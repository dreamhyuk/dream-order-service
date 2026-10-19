package com.dreamhyuk.dream_order.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 마트 장바구니. 음식 장바구니(RedisCart)와 달리 가게에 묶이지 않는다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MartCart {

    private String customerId;
    private List<MartCartItem> items = new ArrayList<>();

    public static MartCart createEmptyCart(String customerId) {
        return new MartCart(customerId, new ArrayList<>());
    }

    //이미 담긴 상품이면 수량만 늘린다
    public void addCartItem(MartCartItem cartItem) {
        this.items.stream()
                .filter(item -> item.getProductId().equals(cartItem.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addCount(cartItem.getCount()),
                        () -> this.items.add(cartItem)
                );
    }

    public void removeCartItem(Long productId) {
        boolean removed = this.items.removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new IllegalArgumentException("장바구니에 해당 상품이 존재하지 않습니다.");
        }
    }
}
