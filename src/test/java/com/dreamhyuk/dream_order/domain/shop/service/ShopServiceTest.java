package com.dreamhyuk.dream_order.domain.shop.service;

import com.dreamhyuk.dream_order.domain.shop.ShopDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShopServiceTest {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    void saveMockData() {
        // 1. 첫 번째 가게 (치킨 카테고리)
        ShopDocument shop1 = ShopDocument.builder()
                .id("1")
                .name("네네치킨 강남점")
                .categories(List.of(
                        new ShopDocument.CategoryInfo("CHICKEN", "치킨"),
                        new ShopDocument.CategoryInfo("FAST_FOOD", "패스트푸드")
                ))
                .build();

        // 2. 두 번째 가게 (피자 카테고리)
        ShopDocument shop2 = ShopDocument.builder()
                .id("2")
                .name("도미노피자 서초점")
                .categories(List.of(
                        new ShopDocument.CategoryInfo("PIZZA", "피자")
                ))
                .build();

        // 3. 저장 실행 (indexName "shops"에 저장됨)
        elasticsearchOperations.save(shop1);
        elasticsearchOperations.save(shop2);

        System.out.println("가짜 데이터 저장 완료!");
    }

}