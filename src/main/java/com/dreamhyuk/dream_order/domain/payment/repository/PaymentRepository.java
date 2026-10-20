package com.dreamhyuk.dream_order.domain.payment.repository;

import com.dreamhyuk.dream_order.domain.payment.Payment;
import com.dreamhyuk.dream_order.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);

    //주문 목록의 결제 여부를 한 번에 확인하기 위한 조회
    @Query("select p.orderId from Payment p where p.orderId in :orderIds and p.status = :status")
    List<Long> findOrderIdsByOrderIdInAndStatus(@Param("orderIds") Collection<Long> orderIds,
                                                @Param("status") PaymentStatus status);
}
