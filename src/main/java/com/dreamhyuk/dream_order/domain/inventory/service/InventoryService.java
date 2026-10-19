package com.dreamhyuk.dream_order.domain.inventory.service;

import com.dreamhyuk.dream_order.domain.inventory.Inventory;
import com.dreamhyuk.dream_order.domain.inventory.InventoryReservation;
import com.dreamhyuk.dream_order.domain.inventory.dto.InventoryResponseDto;
import com.dreamhyuk.dream_order.domain.inventory.repository.InventoryRepository;
import com.dreamhyuk.dream_order.domain.inventory.repository.InventoryReservationRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 재고 선점(reserve) -> 확정(confirm) / 해제(release).
 * 다른 도메인의 엔티티를 받지 않고 orderId, productId, 수량만 주고받는다.
 * 세 메서드 모두 같은 주문에 대해 여러 번 호출돼도 수량이 한 번만 바뀐다(멱등).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    /**
     * 주문 생성 시: 재고 선점
     * 하나라도 부족하면 예외를 던져 호출한 쪽 트랜잭션 전체를 롤백시킨다.
     */
    @Transactional
    public void reserve(Long orderId, Map<Long, Integer> productCounts) {
        //이미 선점한 주문이면 다시 차감하지 않는다
        if (reservationRepository.existsByOrderId(orderId)) {
            return;
        }

        //여러 주문이 같은 상품들을 서로 다른 순서로 잠그면 데드락이 나므로 productId 오름차순으로 처리한다
        for (Map.Entry<Long, Integer> entry : new TreeMap<>(productCounts).entrySet()) {
            Long productId = entry.getKey();
            int count = entry.getValue();

            if (inventoryRepository.reserve(productId, count) == 0) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            reservationRepository.save(InventoryReservation.createReservation(orderId, productId, count));
        }
    }

    /**
     * 결제 완료 시: 선점 확정
     */
    @Transactional
    public void confirm(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderIdOrderByProductId(orderId);

        for (InventoryReservation reservation : reservations) {
            //이미 확정됐거나 해제된 내역은 건너뛴다
            if (!reservation.isReserved()) {
                continue;
            }

            inventoryRepository.confirm(reservation.getProductId(), reservation.getQuantity());
            reservation.confirm();
        }
    }

    /**
     * 주문 취소 시: 선점 해제 (이미 확정된 수량은 가용 재고로 복구)
     */
    @Transactional
    public void release(Long orderId) {
        List<InventoryReservation> reservations = reservationRepository.findAllByOrderIdOrderByProductId(orderId);

        for (InventoryReservation reservation : reservations) {
            if (reservation.isReserved()) {
                inventoryRepository.releaseReserved(reservation.getProductId(), reservation.getQuantity());
            } else if (reservation.isConfirmed()) {
                inventoryRepository.restore(reservation.getProductId(), reservation.getQuantity());
            } else {
                //이미 해제된 내역은 건너뛴다
                continue;
            }

            reservation.release();
        }
    }

    /**
     * 상품 등록 시: 재고 행 생성
     */
    @Transactional
    public void create(Long productId, int quantity) {
        inventoryRepository.save(Inventory.createInventory(productId, quantity));
    }

    /**
     * 가용 재고를 설정한다. 재고 행이 없는 상품이면 새로 만든다.
     */
    @Transactional
    public void updateQuantity(Long productId, int quantity) {
        if (inventoryRepository.updateQuantity(productId, quantity) == 0) {
            inventoryRepository.save(Inventory.createInventory(productId, quantity));
        }
    }

    /**
     * 여러 상품의 가용 재고를 한 번에 조회한다 (Key: productId, Value: 가용 수량).
     * 재고 행이 없는 상품은 결과에 들어가지 않는다.
     */
    public Map<Long, Integer> getQuantities(Collection<Long> productIds) {
        return inventoryRepository.findAllByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(Inventory::getProductId, Inventory::getQuantity));
    }

    public InventoryResponseDto getInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));

        return InventoryResponseDto.from(inventory);
    }
}
