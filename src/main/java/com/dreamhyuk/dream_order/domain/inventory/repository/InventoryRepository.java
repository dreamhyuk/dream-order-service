package com.dreamhyuk.dream_order.domain.inventory.repository;

import com.dreamhyuk.dream_order.domain.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 수량 변경은 모두 원자적 UPDATE 로 처리한다.
 * 반환값은 변경된 행 수이며, 0이면 조건을 만족하지 못한 것이다.
 */
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    List<Inventory> findAllByProductIdIn(Collection<Long> productIds);

    // 선점: 가용 재고가 충분할 때만 가용 -> 선점으로 옮긴다
    @Modifying
    @Query("update Inventory i set i.quantity = i.quantity - :count, i.reservedQuantity = i.reservedQuantity + :count " +
            "where i.productId = :productId and i.quantity >= :count")
    int reserve(@Param("productId") Long productId, @Param("count") int count);

    // 확정: 선점 수량을 차감한다 (가용 재고는 선점 때 이미 줄었다)
    @Modifying
    @Query("update Inventory i set i.reservedQuantity = i.reservedQuantity - :count " +
            "where i.productId = :productId and i.reservedQuantity >= :count")
    int confirm(@Param("productId") Long productId, @Param("count") int count);

    // 선점 해제: 선점 -> 가용으로 되돌린다
    @Modifying
    @Query("update Inventory i set i.quantity = i.quantity + :count, i.reservedQuantity = i.reservedQuantity - :count " +
            "where i.productId = :productId and i.reservedQuantity >= :count")
    int releaseReserved(@Param("productId") Long productId, @Param("count") int count);

    // 확정분 복구: 이미 확정된 수량을 가용 재고로 되돌린다
    @Modifying
    @Query("update Inventory i set i.quantity = i.quantity + :count where i.productId = :productId")
    int restore(@Param("productId") Long productId, @Param("count") int count);

    // 가용 재고를 직접 설정한다
    @Modifying
    @Query("update Inventory i set i.quantity = :quantity where i.productId = :productId")
    int updateQuantity(@Param("productId") Long productId, @Param("quantity") int quantity);
}
