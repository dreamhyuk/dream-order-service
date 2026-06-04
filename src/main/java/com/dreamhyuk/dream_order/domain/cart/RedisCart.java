package com.dreamhyuk.dream_order.domain.cart;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.FetchType.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedisCart {
    private String customerId;
    private Long shopId;
    private List<RedisCartItem> items = new ArrayList<>();

    public static RedisCart createEmptyCart(String customerId) {
        return new RedisCart(customerId, null, new ArrayList<>());
    }

    public void addCartItem(RedisCartItem newItem, Long targetShopId) {
        if (this.shopId != null && !this.shopId.equals(targetShopId)) {
            throw new IllegalArgumentException("장바구니에는 같은 가게의 메뉴만 담을 수 있습니다.");
        }
        if (this.shopId == null) {
            this.shopId = targetShopId;
        }

        this.items.stream()
                .filter(item -> item.getMenuId().equals(newItem.getMenuId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addCount(newItem.getCount()),
                        () -> this.items.add(newItem)
                );
    }
}