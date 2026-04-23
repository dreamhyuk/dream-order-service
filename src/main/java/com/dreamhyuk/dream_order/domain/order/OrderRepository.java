package com.dreamhyuk.dream_order.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByCustomerId(Long customerId, Pageable pageable);

    @Query("select o from Order o where o.customer.id = :customerId and o.status not in (OrderStatus.COMP, OrderStatus.CANCEL)")
    List<Order> findActiveOrders(Long customerId);

    @Query("select o from Order o join fetch o.shop where o.id = :orderId")
    Optional<Order> findWithShopById(@Param("orderId") Long orderId);

}
