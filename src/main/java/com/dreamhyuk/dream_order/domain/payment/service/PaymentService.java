package com.dreamhyuk.dream_order.domain.payment.service;

import com.dreamhyuk.dream_order.domain.payment.Payment;
import com.dreamhyuk.dream_order.domain.payment.PaymentStatus;
import com.dreamhyuk.dream_order.domain.payment.repository.PaymentRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 가짜 결제. 외부 PG 를 호출하지 않고 항상 성공한다.
 * 주문 엔티티를 받지 않고 orderId, 금액만 주고받는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 처리. 이미 결제된 주문이면 아무것도 하지 않는다(멱등).
     */
    @Transactional
    public void pay(Long orderId, int amount) {
        Optional<Payment> existing = paymentRepository.findByOrderId(orderId);

        if (existing.isPresent()) {
            if (existing.get().isPaid()) {
                return;
            }
            //취소된 결제가 있는 주문은 다시 결제할 수 없다
            throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE);
        }

        paymentRepository.save(Payment.createPaid(orderId, amount));
    }

    /**
     * 결제 취소. 결제된 내역이 없으면 아무것도 하지 않는다.
     */
    @Transactional
    public void cancel(Long orderId) {
        paymentRepository.findByOrderId(orderId)
                .filter(Payment::isPaid)
                .ifPresent(Payment::cancel);
    }

    public boolean isPaid(Long orderId) {
        return paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PAID);
    }
}
