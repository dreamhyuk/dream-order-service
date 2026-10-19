package com.dreamhyuk.dream_order.domain.inventory.repository;

import com.dreamhyuk.dream_order.domain.inventory.InventoryReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    boolean existsByOrderId(Long orderId);

    // 같은 주문에 결제(확정)와 취소(해제)가 동시에 들어와도 한쪽씩 처리되도록 잠근다.
    // 데드락을 피하기 위해 항상 productId 오름차순으로 읽는다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<InventoryReservation> findAllByOrderIdOrderByProductId(Long orderId);
}
