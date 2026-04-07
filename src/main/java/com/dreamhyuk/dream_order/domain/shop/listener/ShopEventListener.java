package com.dreamhyuk.dream_order.domain.shop.listener;

import com.dreamhyuk.dream_order.domain.menu.repository.MenuRepository;
import com.dreamhyuk.dream_order.domain.shop.service.ShopSearchSyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class ShopEventListener {

    private final ShopSearchSyncService syncService;
    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용

    @KafkaListener(topics = "dbserver.mydb.shops", groupId = "shop-sync-group")
    public void handleShopCreated(String message) {
        try {
            // Debezium JSON에서 id 추출 (payload -> after -> id)
            JsonNode root = objectMapper.readTree(message);
            Long shopId = root.path("payload").path("after").path("id").asLong();

            log.info("상점 동기화 시작: shopId={}", shopId);
            syncService.sync(shopId);
        } catch (Exception e) {
            log.error("상점 메시지 파싱 에러", e);
        }
    }

    @KafkaListener(topics = "dbserver.mydb.menus", groupId = "menu-sync-group")
    public void handleMenuCreated(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode after = root.path("payload").path("after");

            // Debezium 메시지에 이미 shop_id가 있다면 바로 사용 (조인 쿼리 생략 가능)
            Long shopId = after.path("shop_id").asLong();

            // 만약 메시지에 shop_id가 없고 menu_id만 있다면 기존처럼 레포지토리 조회
            if (shopId == 0) {
                Long menuId = after.path("id").asLong();
                shopId = menuRepository.findShopIdByMenuId(menuId)
                        .orElseThrow(() -> new RuntimeException("상점 정보를 찾을 수 없습니다."));
            }

            log.info("메뉴 변경으로 인한 상점 동기화: shopId={}", shopId);
            syncService.sync(shopId);
        } catch (Exception e) {
            log.error("메뉴 메시지 파싱 에러", e);
        }
    }
}

/*public class ShopEventListener {

    private final ShopSearchSyncService syncService;
    private final MenuRepository menuRepository;

    @KafkaListener(topics = "dbserver.mydb.shops", groupId = "shop-sync-group")
    public void handleShopCreated(String shopId) {
        syncService.sync(Long.parseLong(shopId));
    }

    @KafkaListener(topics = "dbserver.mydb.menus", groupId = "menu-sync-group")
    public void handleMenuCreated(String menuId) {
        // 메뉴가 추가되어도 결국 해당 가게(Shop)의 도큐먼트를 갱신해야 하므로
        Long shopId = menuRepository.findShopIdByMenuId(Long.parseLong(menuId))
                .orElseThrow(() -> new RuntimeException("상점 정보를 찾을 수 없는 메뉴입니다. ID: " + menuId));

        syncService.sync(shopId);
    }
}*/

