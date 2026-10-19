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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryServiceConcurrencyTest {

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("재고 50개에 100명이 동시에 1개씩 주문하면 50명만 성공한다")
    void reserve_concurrently() throws InterruptedException {
        Long productId = 900_001L;
        int stock = 50;
        int threadCount = 100;
        inventoryService.create(productId, stock);

        //ready 래치가 풀리려면 모든 작업이 동시에 대기할 수 있어야 하므로 스레드 수를 작업 수와 같게 둔다
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();
        AtomicInteger unexpected = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            Long orderId = 900_000L + i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    inventoryService.reserve(orderId, Map.of(productId, 1));
                    success.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == ErrorCode.OUT_OF_STOCK) {
                        outOfStock.incrementAndGet();
                    } else {
                        unexpected.incrementAndGet();
                    }
                } catch (Exception e) {
                    unexpected.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        //모든 스레드가 준비된 뒤 한꺼번에 출발시킨다
        ready.await();
        start.countDown();
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        InventoryResponseDto inventory = inventoryService.getInventory(productId);

        assertThat(unexpected.get()).isZero();
        assertThat(success.get()).isEqualTo(stock);
        assertThat(outOfStock.get()).isEqualTo(threadCount - stock);
        assertThat(inventory.getQuantity()).isZero();
        assertThat(inventory.getReservedQuantity()).isEqualTo(stock);
    }
}
