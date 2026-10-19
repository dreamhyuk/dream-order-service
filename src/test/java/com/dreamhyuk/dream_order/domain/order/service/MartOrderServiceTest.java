package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.cart.MartCart;
import com.dreamhyuk.dream_order.domain.cart.MartCartItem;
import com.dreamhyuk.dream_order.domain.cart.service.MartCartService;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.delivery.DeliveryStatus;
import com.dreamhyuk.dream_order.domain.inventory.dto.InventoryResponseDto;
import com.dreamhyuk.dream_order.domain.inventory.service.InventoryService;
import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
import com.dreamhyuk.dream_order.domain.order.OrderStatus;
import com.dreamhyuk.dream_order.domain.order.OrderType;
import com.dreamhyuk.dream_order.domain.order.dto.OrderDetailResponseDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderResponseDto;
import com.dreamhyuk.dream_order.domain.payment.service.PaymentService;
import com.dreamhyuk.dream_order.domain.product.Product;
import com.dreamhyuk.dream_order.domain.product.ProductCategory;
import com.dreamhyuk.dream_order.domain.product.repository.ProductCategoryRepository;
import com.dreamhyuk.dream_order.domain.product.repository.ProductRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class MartOrderServiceTest {

    //이메일이 테스트끼리 겹치지 않도록 매번 새 번호를 쓴다
    private static final AtomicInteger SEQ = new AtomicInteger();

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    //장바구니는 Redis에 있으므로 가짜로 대체한다
    @MockBean
    private MartCartService martCartService;

    @Autowired private MartOrderService martOrderService;
    @Autowired private OrderService orderService;
    @Autowired private InventoryService inventoryService;
    @Autowired private PaymentService paymentService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductCategoryRepository productCategoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    private Customer customer;
    private Product water;
    private Product tissue;

    @BeforeEach
    void setUp() {
        int seq = SEQ.incrementAndGet();

        customer = customerRepository.save(Customer.builder()
                .email("mart-test-customer" + seq + "@test.com")
                .password("1111")
                .username("martCustomer" + seq)
                .role(MemberRole.CUSTOMER)
                .build());

        ProductCategory category = productCategoryRepository.save(ProductCategory.createProductCategory("생활용품", 1));
        water = productRepository.save(Product.createProduct(category, "생수 2L", 1200));
        tissue = productRepository.save(Product.createProduct(category, "물티슈 100매", 2000));

        inventoryService.create(water.getId(), 10);
        inventoryService.create(tissue.getId(), 10);
    }

    @Test
    @DisplayName("마트 주문을 생성하면 상품을 ID와 주문 시점의 이름·가격으로 저장하고 재고를 선점한다")
    void saveOrderFromCart() {
        Long orderId = placeOrder(3, 1);

        transactionTemplate.executeWithoutResult(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow();

            assertThat(order.getOrderType()).isEqualTo(OrderType.MART);
            assertThat(order.getSellerId()).isNull();
            assertThat(order.getSellerName()).isEqualTo("B마트");
            assertThat(order.getCustomerId()).isEqualTo(customer.getId());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalOrderPrice()).isEqualTo(1200 * 3 + 2000);
            assertThat(order.getOrderItems())
                    .extracting("itemId", "itemName", "orderPrice", "count")
                    .containsExactlyInAnyOrder(
                            Tuple.tuple(water.getId(), "생수 2L", 1200, 3),
                            Tuple.tuple(tissue.getId(), "물티슈 100매", 2000, 1));
        });

        assertInventory(water.getId(), 7, 3);
        assertInventory(tissue.getId(), 9, 1);
        verify(martCartService).clearCart(customer.getId());
    }

    @Test
    @DisplayName("마트 주문도 고객 주문 목록과 상세에서 조회되고, 상품 이름이 바뀌어도 주문 시점 값이 나온다")
    void findMartOrder() throws Exception {
        Long orderId = placeOrder(2, 0);

        water.update("이름이 바뀐 생수", 9900);
        productRepository.save(water);

        List<OrderResponseDto> orders = orderService.findActiveOrders(customer.getId());
        OrderDetailResponseDto detail = orderService.findOrderDetail(orderId, customer.getId());

        assertThat(orders).extracting("orderId", "shopName", "orderTitle")
                .containsExactly(Tuple.tuple(orderId, "B마트", "생수 2L"));
        assertThat(detail.getShopName()).isEqualTo("B마트");
        assertThat(detail.getTotalPrice()).isEqualTo(2400);
        assertThat(detail.getOrderItems()).extracting("menuName", "orderPrice", "count")
                .containsExactly(Tuple.tuple("생수 2L", 1200, 2));
    }

    @Test
    @DisplayName("재고가 부족하면 주문이 생성되지 않고 장바구니도 비우지 않는다")
    void saveOrderFromCart_outOfStock() {
        long before = orderRepository.count();

        assertThatThrownBy(() -> placeOrder(11, 1))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUT_OF_STOCK);

        assertThat(orderRepository.count()).isEqualTo(before);
        assertInventory(water.getId(), 10, 0);
        assertInventory(tissue.getId(), 10, 0);
        verify(martCartService, never()).clearCart(customer.getId());
    }

    @Test
    @DisplayName("판매 중지된 상품이 있으면 주문할 수 없다")
    void saveOrderFromCart_notOnSale() {
        tissue.stopSale();
        productRepository.save(tissue);

        assertThatThrownBy(() -> placeOrder(1, 1))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_ON_SALE);

        assertInventory(water.getId(), 10, 0);
    }

    @Test
    @DisplayName("장바구니에 담은 뒤 가격이 바뀐 상품이 있으면 주문할 수 없다")
    void saveOrderFromCart_priceChanged() {
        water.update("생수 2L", 1500);
        productRepository.save(water);

        //장바구니에는 바뀌기 전 가격(1200원)으로 담겨 있다
        assertThatThrownBy(() -> placeOrder(1, 0))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_PRICE_CHANGED);

        assertInventory(water.getId(), 10, 0);
    }

    @Test
    @DisplayName("빈 장바구니로는 주문할 수 없다")
    void saveOrderFromCart_emptyCart() {
        assertThatThrownBy(() -> placeOrder(0, 0))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CART_EMPTY);
    }

    @Test
    @DisplayName("결제하면 선점한 재고가 확정되고 주문이 자동으로 접수된다")
    void payOrder() {
        Long orderId = placeOrder(3, 0);

        orderService.payOrder(orderId, customer.getId());

        assertInventory(water.getId(), 7, 0);
        assertThat(paymentService.isPaid(orderId)).isTrue();
        transactionTemplate.executeWithoutResult(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
            assertThat(order.getDelivery().getStatus()).isEqualTo(DeliveryStatus.PENDING);
        });
    }

    @Test
    @DisplayName("주문을 취소하면 재고가 복구된다 (결제 전, 결제 후 모두)")
    void cancelOrder() {
        Long unpaidOrderId = placeOrder(2, 0);
        orderService.cancelOrder(unpaidOrderId, customer.getId());
        assertInventory(water.getId(), 10, 0);

        Long paidOrderId = placeOrder(3, 0);
        orderService.payOrder(paidOrderId, customer.getId());
        assertInventory(water.getId(), 7, 0);

        orderService.cancelOrder(paidOrderId, customer.getId());
        assertInventory(water.getId(), 10, 0);

        assertThat(orderRepository.findById(paidOrderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(paymentService.isPaid(paidOrderId)).isFalse();
    }

    //생수 waterCount 개, 물티슈 tissueCount 개를 장바구니에 담은 것으로 하고 주문한다 (0이면 담지 않는다)
    private Long placeOrder(int waterCount, int tissueCount) {
        List<MartCartItem> items = new ArrayList<>();
        if (waterCount > 0) {
            items.add(new MartCartItem(water.getId(), "생수 2L", 1200, waterCount));
        }
        if (tissueCount > 0) {
            items.add(new MartCartItem(tissue.getId(), "물티슈 100매", 2000, tissueCount));
        }

        given(martCartService.getCart(customer.getId()))
                .willReturn(new MartCart(String.valueOf(customer.getId()), items));

        return martOrderService.saveOrderFromCart(customer.getId(), OrderCommand.CreateMartFromCart.builder()
                .directAddress(Address.of("Seoul", "sungnae", "12345"))
                .build());
    }

    private void assertInventory(Long productId, int quantity, int reservedQuantity) {
        InventoryResponseDto inventory = inventoryService.getInventory(productId);

        assertThat(inventory.getQuantity()).isEqualTo(quantity);
        assertThat(inventory.getReservedQuantity()).isEqualTo(reservedQuantity);
    }
}
