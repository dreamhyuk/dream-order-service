package com.dreamhyuk.dream_order.domain.inventory.service;

import com.dreamhyuk.dream_order.domain.inventory.dto.InventoryResponseDto;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class InventoryServiceTest {

    //테스트끼리 재고 행이 겹치지 않도록 매번 새 ID를 쓴다
    private static final AtomicLong ID = new AtomicLong(1000);

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("선점하면 가용 재고가 줄고 선점 수량이 늘어난다")
    void reserve() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 10);

        inventoryService.reserve(orderId, Map.of(productId, 3));

        assertInventory(productId, 7, 3);
    }

    @Test
    @DisplayName("재고가 부족하면 OUT_OF_STOCK 예외가 발생하고 수량은 그대로다")
    void reserve_outOfStock() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 2);

        assertThatThrownBy(() -> inventoryService.reserve(orderId, Map.of(productId, 3)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUT_OF_STOCK);

        assertInventory(productId, 2, 0);
    }

    @Test
    @DisplayName("여러 상품 중 하나라도 부족하면 앞서 선점한 상품도 롤백된다")
    void reserve_rollbackAll() {
        Long enoughProductId = ID.incrementAndGet();
        Long shortProductId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(enoughProductId, 10);
        inventoryService.create(shortProductId, 1);

        assertThatThrownBy(() -> inventoryService.reserve(orderId, Map.of(enoughProductId, 5, shortProductId, 2)))
                .isInstanceOf(BusinessException.class);

        assertInventory(enoughProductId, 10, 0);
        assertInventory(shortProductId, 1, 0);
    }

    @Test
    @DisplayName("같은 주문으로 선점을 두 번 요청해도 한 번만 차감된다")
    void reserve_idempotent() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 10);

        inventoryService.reserve(orderId, Map.of(productId, 3));
        inventoryService.reserve(orderId, Map.of(productId, 3));

        assertInventory(productId, 7, 3);
    }

    @Test
    @DisplayName("확정하면 선점 수량만 줄고 가용 재고는 그대로다. 두 번 호출해도 결과가 같다")
    void confirm() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 10);
        inventoryService.reserve(orderId, Map.of(productId, 3));

        inventoryService.confirm(orderId);
        inventoryService.confirm(orderId);

        assertInventory(productId, 7, 0);
    }

    @Test
    @DisplayName("선점 상태에서 해제하면 가용 재고가 복구된다. 두 번 호출해도 결과가 같다")
    void release_reserved() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 10);
        inventoryService.reserve(orderId, Map.of(productId, 3));

        inventoryService.release(orderId);
        inventoryService.release(orderId);

        assertInventory(productId, 10, 0);
    }

    @Test
    @DisplayName("확정 상태에서 해제하면 가용 재고가 복구된다. 두 번 호출해도 결과가 같다")
    void release_confirmed() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 10);
        inventoryService.reserve(orderId, Map.of(productId, 3));
        inventoryService.confirm(orderId);

        inventoryService.release(orderId);
        inventoryService.release(orderId);

        assertInventory(productId, 10, 0);
    }

    @Test
    @DisplayName("해제된 선점은 확정해도 수량이 바뀌지 않는다")
    void confirm_afterRelease() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();
        inventoryService.create(productId, 10);
        inventoryService.reserve(orderId, Map.of(productId, 3));
        inventoryService.release(orderId);

        inventoryService.confirm(orderId);

        assertInventory(productId, 10, 0);
    }

    @Test
    @DisplayName("재고 수량을 설정하면 가용 재고만 바뀌고, 재고 행이 없으면 새로 만든다")
    void updateQuantity() {
        Long productId = ID.incrementAndGet();
        Long orderId = ID.incrementAndGet();

        inventoryService.updateQuantity(productId, 5);
        assertInventory(productId, 5, 0);

        inventoryService.reserve(orderId, Map.of(productId, 2));
        inventoryService.updateQuantity(productId, 20);
        assertInventory(productId, 20, 2);
    }

    private void assertInventory(Long productId, int quantity, int reservedQuantity) {
        InventoryResponseDto inventory = inventoryService.getInventory(productId);

        assertThat(inventory.getQuantity()).isEqualTo(quantity);
        assertThat(inventory.getReservedQuantity()).isEqualTo(reservedQuantity);
    }
}
