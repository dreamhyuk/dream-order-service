package com.dreamhyuk.dream_order.domain.shop.service;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuGroupRepository;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopDocument;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopSearchSyncService {

    private final ShopRepository shopRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void sync(Long shopId) {
        // 1. 필요한 모든 데이터 조회 (ID 참조 방식이므로 각각 조회)
        Shop shop = shopRepository.findById(shopId).orElseThrow();
        List<MenuGroup> menuGroups = menuGroupRepository.findByShopId(shopId);

        // 2. 비정규화된 ShopDocument 생성 및 저장
        ShopDocument doc = ShopDocument.from(shop, menuGroups);
        elasticsearchOperations.save(doc);
    }
}
