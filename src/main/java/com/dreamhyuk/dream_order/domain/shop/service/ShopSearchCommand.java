package com.dreamhyuk.dream_order.domain.shop.service;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class ShopSearchCommand {

    private final String keyword;
    private final Long categoryId;

    public ShopSearchCommand(String keyword, Long categoryId) {
        this.keyword = keyword;

        //정책: 키워드가 있으면 카테고리는 무시한다
        this.categoryId = StringUtils.hasText(keyword) ? null : categoryId;
    }

    public static ShopSearchCommand of(String keyword, Long categoryId) {
        return new ShopSearchCommand(keyword, categoryId);
    }
}
