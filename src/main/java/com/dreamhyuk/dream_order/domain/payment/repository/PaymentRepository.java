package com.dreamhyuk.dream_order.domain.payment.repository;

import com.dreamhyuk.dream_order.domain.payment.Payment;
import com.dreamhyuk.dream_order.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
