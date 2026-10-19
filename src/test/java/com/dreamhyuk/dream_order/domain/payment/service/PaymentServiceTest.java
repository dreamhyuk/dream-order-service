package com.dreamhyuk.dream_order.domain.payment.service;

import com.dreamhyuk.dream_order.domain.payment.repository.PaymentRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    //다른 테스트가 만든 실제 주문의 결제와 겹치지 않도록 큰 주문 ID를 쓴다

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제하면 결제 완료 상태가 된다")
    void pay() {
        Long orderId = 800_001L;

        assertThat(paymentService.isPaid(orderId)).isFalse();

        paymentService.pay(orderId, 20000);

        assertThat(paymentService.isPaid(orderId)).isTrue();
        assertThat(paymentRepository.findByOrderId(orderId).orElseThrow().getAmount()).isEqualTo(20000);
    }

    @Test
    @DisplayName("같은 주문을 두 번 결제해도 결제는 한 건만 생긴다")
    void pay_idempotent() {
        Long orderId = 800_002L;

        paymentService.pay(orderId, 20000);
        paymentService.pay(orderId, 20000);

        assertThat(paymentRepository.findAll())
                .filteredOn(payment -> payment.getOrderId().equals(orderId))
                .hasSize(1);
    }

    @Test
    @DisplayName("결제를 취소하면 결제 완료 상태가 아니게 되고, 다시 결제할 수 없다")
    void cancel() {
        Long orderId = 800_003L;
        paymentService.pay(orderId, 20000);

        paymentService.cancel(orderId);

        assertThat(paymentService.isPaid(orderId)).isFalse();
        assertThatThrownBy(() -> paymentService.pay(orderId, 20000))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_PAYABLE);
    }

    @Test
    @DisplayName("결제 내역이 없는 주문을 취소해도 예외가 발생하지 않는다")
    void cancel_withoutPayment() {
        paymentService.cancel(800_004L);

        assertThat(paymentService.isPaid(800_004L)).isFalse();
    }
}
