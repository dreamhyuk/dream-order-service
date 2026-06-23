package com.dreamhyuk.dream_order.domain.cart;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedisCart {

    private String customerId;
    private Long shopId;
    private String shopName;
    private List<RedisCartItem> items = new ArrayList<>();

    public static RedisCart createEmptyCart(String customerId) {
        return new RedisCart(customerId, null, null, new ArrayList<>());
    }

    public void addCartItem(RedisCartItem cartItem, Long shopId, String shopName) {
        if (this.shopId != null && !this.shopId.equals(shopId)) {
            throw new IllegalArgumentException("장바구니에는 같은 가게의 메뉴만 담을 수 있습니다.");
        }
        if (this.shopId == null) {
            this.shopId = shopId;
            this.shopName = shopName;
        }

        this.items.stream()
                .filter(item -> item.getMenuId().equals(cartItem.getMenuId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addCount(cartItem.getCount()),
                        () -> this.items.add(cartItem)
                );
    }

    public void removeCartItem(Long menuId) {
        // 1. 리스트를 순회하며 menuId가 일치하는 아이템을 찾아 즉시 제거합니다.
        // (제거에 성공하면 true, 없어서 실패하면 false를 반환합니다.)
        boolean removed = this.items.removeIf(item -> item.getMenuId().equals(menuId));

        // 2. 만약 지우려고 한 메뉴가 장바구니에 없었다면 예외를 던진다
        if (!removed) {
            throw new IllegalArgumentException("장바구니에 해당 메뉴가 존재하지 않습니다.");
        }

        // 3. 🔥 [중요 디테일] 메뉴를 지웠는데 장바구니 리스트가 완전히 텅 비었다면?
        // 이제 다른 가게 메뉴도 새로 담을 수 있어야 하므로, 묶여있던가게 ID(shopId)를 null로 초기화합니다.
        if (this.items.isEmpty()) {
            this.shopId = null;
            this.shopName = null;
        }
    }
}