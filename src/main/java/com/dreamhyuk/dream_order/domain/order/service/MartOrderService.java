package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.cart.MartCart;
import com.dreamhyuk.dream_order.domain.cart.MartCartItem;
import com.dreamhyuk.dream_order.domain.cart.service.MartCartService;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.delivery.Delivery;
import com.dreamhyuk.dream_order.domain.inventory.service.InventoryService;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderItem;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
import com.dreamhyuk.dream_order.domain.order.OrderType;
import com.dreamhyuk.dream_order.domain.product.Product;
import com.dreamhyuk.dream_order.domain.product.repository.ProductRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 마트 주문 생성. 결제, 취소, 조회는 음식 주문과 함께 OrderService 를 쓴다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MartOrderService {

    //마트는 가게가 없으므로 주문 내역에 보여줄 판매처 이름을 고정한다
    private static final String MART_SELLER_NAME = "B마트";

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final MartCartService martCartService;
    private final InventoryService inventoryService;
    private final DeliveryAddressResolver deliveryAddressResolver;

    @Transactional
    public Long saveOrderFromCart(Long customerId, OrderCommand.CreateMartFromCart command) {
        // 1. 고객 확인
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 2. Redis에서 마트 장바구니 조회
        MartCart cart = martCartService.getCart(customerId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        Map<Long, MartCartItem> cartItemMap = cart.getItems().stream()
                .collect(Collectors.toMap(MartCartItem::getProductId, item -> item));

        // 3. 상품 조회 및 검증 (존재 여부, 판매 상태, 가격 변동)
        List<Product> products = productRepository.findAllById(cartItemMap.keySet());
        if (products.size() != cartItemMap.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        for (Product product : products) {
            if (!product.isOnSale()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
            }

            // 장바구니에 담았던 시점의 가격과 RDB 최신 가격 대조 (위변조 방지)
            if (product.getPrice() != cartItemMap.get(product.getId()).getPrice()) {
                throw new BusinessException(ErrorCode.PRODUCT_PRICE_CHANGED);
            }
        }

        // 4. 배송 정보 결정 (마트 주문은 항상 배달)
        Address address = deliveryAddressResolver.resolve(customerId, command.getMyAddressId(), command.getDirectAddress());
        Delivery delivery = Delivery.createDelivery(address, DeliveryType.DREAM_DELIVERY);

        // 5. 주문 생성 및 DB 저장 (상품 이름과 가격은 주문 시점 값으로 복사한다)
        List<OrderItem> orderItems = products.stream()
                .map(p -> OrderItem.createOrderItem(p.getId(), p.getName(), p.getPrice(), cartItemMap.get(p.getId()).getCount()))
                .toList();

        Order order = Order.createOrder(OrderType.MART, null, MART_SELLER_NAME, customerId,
                delivery, DeliveryType.DREAM_DELIVERY, orderItems);
        orderRepository.save(order);

        // 6. 재고 선점 (하나라도 부족하면 예외가 발생해 주문 전체가 롤백되고 장바구니는 유지된다)
        Map<Long, Integer> productCounts = cart.getItems().stream()
                .collect(Collectors.toMap(MartCartItem::getProductId, MartCartItem::getCount));
        inventoryService.reserve(order.getId(), productCounts);

        // 7. 주문이 저장되었으므로 마트 장바구니를 비운다
        martCartService.clearCart(customerId);

        return order.getId();
    }
}
