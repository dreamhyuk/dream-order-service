package com.dreamhyuk.dream_order.domain.shop.service;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class ShopSearchCommand {

    private final String keyword;
    private final String categoryType;

    public ShopSearchCommand(String keyword, String categoryType) {
        this.keyword = keyword;

        //정책: 키워드가 있으면 카테고리는 무시한다
        this.categoryType = StringUtils.hasText(keyword) ? null : categoryType;
    }

    public static ShopSearchCommand of(String keyword, String categoryType) {
        return new ShopSearchCommand(keyword, categoryType);
    }
}
