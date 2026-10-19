package com.dreamhyuk.dream_order.domain.inventory;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품별 재고.
 * 추후 별도 서비스로 분리할 수 있도록 Product 를 객체로 참조하지 않고 productId 만 가진다.
 * 수량 변경은 동시성 문제를 피하기 위해 엔티티 변경 감지가 아니라 InventoryRepository 의 조건부 UPDATE 로만 한다.
 */
@Entity
@Table(name = "inventories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    //지금 주문 가능한 가용 재고
    @Column(nullable = false)
    private int quantity;

    //선점됐지만 아직 확정(결제 완료)되지 않은 수량
    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    //== 생성 메서드 ==//
    public static Inventory createInventory(Long productId, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }

        Inventory inventory = new Inventory();
        inventory.productId = productId;
        inventory.quantity = quantity;
        inventory.reservedQuantity = 0;

        return inventory;
    }
}
