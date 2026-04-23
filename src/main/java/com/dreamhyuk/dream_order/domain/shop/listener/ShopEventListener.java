package com.dreamhyuk.dream_order.domain.shop.listener;

import com.dreamhyuk.dream_order.domain.menu.repository.MenuGroupRepository;
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
    private final MenuGroupRepository menuGroupRepository;
    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용

    @KafkaListener(topics = "dbserver.mydb.shops", groupId = "shop-sync-group")
    public void handleShopEvent(String message) {
        try {
            // Debezium JSON에서 읽어서 메모리에 '트리'로 펼침
            JsonNode root = objectMapper.readTree(message);
            // 트리에서 탐색(payload -> op)해서 String 타입으로 변환
            String op = root.path("payload").path("op").asText(); // c, u, d 중 하나

            // 어떤 경우든 '가장 최신 데이터' 혹은 '방금 전 데이터'에서 ID 추출
            JsonNode data = op.equals("d") ?
                    root.path("payload").path("before") :
                    root.path("payload").path("after");

            Long shopId = data.path("shop_id").asLong();

            if (op.equals("d")) {
                log.info("상점 삭제 감지: shopId={}", shopId);
                syncService.delete(shopId);
            } else {
                log.info("상점 생성/수정 감지: shopId={}", shopId);
                syncService.sync(shopId);
            }
        } catch (Exception e) {
            log.error("상점 메시지 파싱 에러", e);
        }
    }

    @KafkaListener(topics = "dbserver.mydb.menu_groups", groupId = "menu-group-sync-group")
    public void handleMenuGroupEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String op = root.path("payload").path("op").asText();

            JsonNode data = op.equals("d") ?
                    root.path("payload").path("before") :
                    root.path("payload").path("after");

            Long shopId = data.path("shop_id").asLong();

            // 0이면 파싱 실패이므로 로직 중단
            if (shopId == 0) return;

            log.info("메뉴그룹 변경 감지 (op: {}): shopId={}", op, shopId);

            //메뉴그룹이 삭제되었든 생성되었든, 상점의 '현재 상태'를 다시 읽어서 ES에 덮어씌워야 하므로 sync를 호출
            //syncService에서 deleteMenuGrouop() 메서드를 따로 만들 필요가 없다.
            syncService.sync(shopId);
        } catch (Exception e) {
            log.error("메뉴 그룹 메시지 파싱 에러", e);
        }
    }

    @KafkaListener(topics = "dbserver.mydb.menus", groupId = "menu-sync-group")
    public void handleMenuEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String op = root.path("payload").path("op").asText();

            JsonNode data = op.equals("d") ?
                    root.path("payload").path("before") :
                    root.path("payload").path("after");

            Long shopId = data.path("shop_id").asLong();

            if (shopId == 0) return;

            log.info("Menu update으로 인한 Shop sync: shopId={}", shopId);

            //마찬가지로 메뉴가 삭제되든 생성, 업데이트되든 ES에 덮어씌워줘야 하므로 sync만 호출하면 된다.
            syncService.sync(shopId);
        } catch (Exception e) {
            log.error("메뉴 메시지 파싱 에러", e);
        }
    }
}
