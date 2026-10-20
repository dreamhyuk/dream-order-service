package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.cart.RedisCart;
import com.dreamhyuk.dream_order.domain.cart.RedisCartItem;
import com.dreamhyuk.dream_order.domain.cart.service.CartService;
import com.dreamhyuk.dream_order.domain.category.Category;
import com.dreamhyuk.dream_order.domain.category.CategoryRepository;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.delivery.DeliveryStatus;
import com.dreamhyuk.dream_order.domain.inventory.repository.InventoryReservationRepository;
import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import com.dreamhyuk.dream_order.domain.menu.Menu;
import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuGroupRepository;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuRepository;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
import com.dreamhyuk.dream_order.domain.order.OrderStatus;
import com.dreamhyuk.dream_order.domain.order.OrderType;
import com.dreamhyuk.dream_order.domain.order.dto.OrderDetailResponseDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderResponseDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderSummaryResponse;
import com.dreamhyuk.dream_order.domain.payment.service.PaymentService;
import com.dreamhyuk.dream_order.domain.review.service.ReviewCommand;
import com.dreamhyuk.dream_order.domain.review.service.ReviewService;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class OrderServiceTest {

    //카테고리 타입, 이메일이 테스트끼리 겹치지 않도록 매번 새 번호를 쓴다
    private static final AtomicInteger SEQ = new AtomicInteger();

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    //장바구니는 Redis에 있으므로 가짜로 대체한다
    @MockBean
    private CartService cartService;

    @Autowired private OrderService orderService;
    @Autowired private ReviewService reviewService;
    @Autowired private PaymentService paymentService;
    @Autowired private InventoryReservationRepository reservationRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OwnerRepository ownerRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ShopRepository shopRepository;
    @Autowired private MenuGroupRepository menuGroupRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    private Customer customer;
    private Shop shop;
    private Menu chicken;
    private Menu cola;

    @BeforeEach
    void setUp() {
        int seq = SEQ.incrementAndGet();

        customer = customerRepository.save(Customer.builder()
                .email("order-test-customer" + seq + "@test.com")
                .password("1111")
                .username("customer" + seq)
                .role(MemberRole.CUSTOMER)
                .build());

        Owner owner = ownerRepository.save(Owner.builder()
                .email("order-test-owner" + seq + "@test.com")
                .password("1111")
                .businessNumber("000-" + seq)
                .role(MemberRole.OWNER)
                .build());

        Category category = categoryRepository.save(Category.createCategory("ORDER_TEST_" + seq, "치킨", seq));

        shop = shopRepository.save(Shop.createShop(
                owner,
                List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.TAKEOUT),
                List.of(category),
                Address.of("Seoul", "pungnab", "56789"),
                "테스트치킨" + seq));

        MenuGroup group = menuGroupRepository.save(MenuGroup.createMenuGroup("대표메뉴", 1, shop.getId()));
        chicken = menuRepository.save(Menu.createMenu("후라이드치킨", 20000, shop.getId(), group));
        cola = menuRepository.save(Menu.createMenu("코카콜라", 3000, shop.getId(), group));
    }

    @Test
    @DisplayName("주문을 생성하면 가게와 메뉴를 ID와 주문 시점의 이름·가격으로 저장한다")
    void saveOrderFromCart() {
        Long orderId = placeOrder(customer, 2, 1);

        transactionTemplate.executeWithoutResult(status -> {
            Order order = orderRepository.findById(orderId).orElseThrow();

            assertThat(order.getOrderType()).isEqualTo(OrderType.FOOD);
            assertThat(order.getSellerId()).isEqualTo(shop.getId());
            assertThat(order.getSellerName()).isEqualTo(shop.getShopName());
            assertThat(order.getCustomerId()).isEqualTo(customer.getId());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalOrderPrice()).isEqualTo(20000 * 2 + 3000);
            assertThat(order.getOrderItems())
                    .extracting("itemId", "itemName", "orderPrice", "count")
                    .containsExactlyInAnyOrder(
                            org.assertj.core.groups.Tuple.tuple(chicken.getId(), "후라이드치킨", 20000, 2),
                            org.assertj.core.groups.Tuple.tuple(cola.getId(), "코카콜라", 3000, 1));
        });

        //음식 주문은 재고를 선점하지 않는다
        assertThat(reservationRepository.existsByOrderId(orderId)).isFalse();
        verify(cartService).clearCart(customer.getId());
    }

    @Test
    @DisplayName("주문 후 가게 메뉴의 이름과 가격이 바뀌어도 주문 내역은 주문 시점 그대로다")
    void orderKeepsSnapshot() throws Exception {
        Long orderId = placeOrder(customer, 2, 1);

        chicken.update("이름이 바뀐 치킨", 99000);
        menuRepository.save(chicken);

        OrderDetailResponseDto detail = orderService.findOrderDetail(orderId, customer.getId());

        assertThat(detail.getShopName()).isEqualTo(shop.getShopName());
        assertThat(detail.getTotalPrice()).isEqualTo(20000 * 2 + 3000);
        assertThat(detail.getOrderItems())
                .extracting("menuName", "orderPrice", "count", "totalPrice")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("후라이드치킨", 20000, 2, 40000),
                        org.assertj.core.groups.Tuple.tuple("코카콜라", 3000, 1, 3000));
    }

    @Test
    @DisplayName("고객 주문 목록과 진행 중인 주문을 조회한다")
    void findCustomerOrders() {
        Long orderId = placeOrder(customer, 1, 1);

        List<OrderResponseDto> orders = orderService.findAllOrders(customer.getId(), PageRequest.of(0, 10)).getContent();
        List<OrderResponseDto> activeOrders = orderService.findActiveOrders(customer.getId());

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderId()).isEqualTo(orderId);
        assertThat(orders.get(0).getShopName()).isEqualTo(shop.getShopName());
        assertThat(orders.get(0).getOrderTitle()).endsWith("외 1건");
        assertThat(activeOrders).extracting("orderId").containsExactly(orderId);
    }

    @Test
    @DisplayName("사장님 주문 목록은 해당 가게의 주문만 반환한다")
    void findOrdersByShopId() {
        Long orderId = placeOrder(customer, 1, 0);

        List<OrderSummaryResponse> orders = orderService.findOrdersByShopId(shop.getId());
        List<OrderSummaryResponse> otherShopOrders = orderService.findOrdersByShopId(shop.getId() + 100_000);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderId()).isEqualTo(orderId);
        assertThat(orders.get(0).getMenuName()).isEqualTo("후라이드치킨");
        assertThat(otherShopOrders).isEmpty();
    }

    @Test
    @DisplayName("사장님 주문 목록에 결제 여부와 배달 상태가 담긴다")
    void findOrdersByShopId_paidAndDeliveryStatus() {
        Long unpaidOrderId = placeOrder(customer, 1, 0);
        Long paidOrderId = placeOrder(customer, 1, 0);
        orderService.payOrder(paidOrderId, customer.getId());
        orderService.acceptOrder(paidOrderId, 30);
        orderService.startCooking(paidOrderId);
        orderService.completeCooking(paidOrderId);

        List<OrderSummaryResponse> orders = orderService.findOrdersByShopId(shop.getId());

        OrderSummaryResponse unpaid = orders.stream()
                .filter(o -> o.getOrderId().equals(unpaidOrderId)).findFirst().orElseThrow();
        OrderSummaryResponse paid = orders.stream()
                .filter(o -> o.getOrderId().equals(paidOrderId)).findFirst().orElseThrow();

        assertThat(unpaid.isPaid()).isFalse();
        assertThat(unpaid.getDeliveryStatus()).isEqualTo(DeliveryStatus.NONE);
        assertThat(paid.isPaid()).isTrue();
        assertThat(paid.getStatus()).isEqualTo(OrderStatus.PREPARING.name());
        assertThat(paid.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY_FOR_PICKUP);
    }

    @Test
    @DisplayName("품절된 메뉴가 있으면 주문이 생성되지 않고 장바구니도 비우지 않는다")
    void saveOrderFromCart_soldOut() {
        long before = orderRepository.count();

        cola.changeSoldOut(true);
        menuRepository.save(cola);

        assertThatThrownBy(() -> placeOrder(customer, 1, 1))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MENU_SOLD_OUT);

        assertThat(orderRepository.count()).isEqualTo(before);
        verify(cartService, never()).clearCart(customer.getId());
    }

    @Test
    @DisplayName("음식 주문은 결제해도 대기 상태이고, 결제된 주문만 사장님이 접수할 수 있다")
    void payAndAccept() {
        Long orderId = placeOrder(customer, 2, 0);

        assertThatThrownBy(() -> orderService.acceptOrder(orderId, 30))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_REQUIRED);

        orderService.payOrder(orderId, customer.getId());
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PENDING);

        Order accepted = orderService.acceptOrder(orderId, 30);
        assertThat(accepted.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("주문을 취소하면 취소 상태가 되고 진행 중인 주문에서 빠진다 (결제 전, 결제 후 모두)")
    void cancelOrder() {
        Long unpaidOrderId = placeOrder(customer, 2, 0);
        orderService.cancelOrder(unpaidOrderId, customer.getId());

        Long paidOrderId = placeOrder(customer, 3, 0);
        orderService.payOrder(paidOrderId, customer.getId());
        orderService.cancelOrder(paidOrderId, customer.getId());

        assertThat(orderRepository.findById(unpaidOrderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(orderRepository.findById(paidOrderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(paymentService.isPaid(paidOrderId)).isFalse();
        assertThat(orderService.findActiveOrders(customer.getId())).isEmpty();
    }

    @Test
    @DisplayName("남의 주문은 결제하거나 취소할 수 없다")
    void otherCustomerOrder() {
        Long orderId = placeOrder(customer, 1, 0);
        Long otherCustomerId = customer.getId() + 100_000;

        assertThatThrownBy(() -> orderService.payOrder(orderId, otherCustomerId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);

        assertThatThrownBy(() -> orderService.cancelOrder(orderId, otherCustomerId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문한 가게에 리뷰를 작성할 수 있다")
    void saveReview() {
        Long orderId = placeOrder(customer, 1, 0);

        Long reviewId = reviewService.saveReview(ReviewCommand.Create.builder()
                .customerId(customer.getId())
                .orderId(orderId)
                .score(4.5)
                .content("맛있어요")
                .build());

        assertThat(reviewId).isNotNull();
    }

    //치킨 chickenCount 개, 콜라 colaCount 개를 장바구니에 담은 것으로 하고 주문한다 (0이면 담지 않는다)
    private Long placeOrder(Customer customer, int chickenCount, int colaCount) {
        List<RedisCartItem> items = new java.util.ArrayList<>();
        if (chickenCount > 0) {
            items.add(new RedisCartItem(chicken.getId(), "후라이드치킨", 20000, chickenCount));
        }
        if (colaCount > 0) {
            items.add(new RedisCartItem(cola.getId(), "코카콜라", 3000, colaCount));
        }

        given(cartService.getCart(customer.getId()))
                .willReturn(new RedisCart(String.valueOf(customer.getId()), shop.getId(), shop.getShopName(), items));

        return orderService.saveOrderFromCart(customer.getId(), OrderCommand.CreateFromCart.builder()
                .deliveryType(DeliveryType.DREAM_DELIVERY)
                .directAddress(Address.of("Seoul", "sungnae", "12345"))
                .build());
    }
}
