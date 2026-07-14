package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.cart.RedisCart;
import com.dreamhyuk.dream_order.domain.cart.RedisCartItem;
import com.dreamhyuk.dream_order.domain.cart.service.CartService;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddress;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddressRepository;
import com.dreamhyuk.dream_order.domain.menu.Menu;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuRepository;
import com.dreamhyuk.dream_order.domain.delivery.Delivery;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderItem;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
import com.dreamhyuk.dream_order.domain.order.dto.OrderDetailResponseDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderResponseDto;
import com.dreamhyuk.dream_order.domain.order.dto.OrderSummaryResponse;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;
    private final MyAddressRepository myAddressRepository;
    private final CartService cartService;

    public List<OrderSummaryResponse> findOrdersByShopId(Long shopId) {
        List<Order> orders = orderRepository.findByShopId(shopId);

        return orders.stream()
                .map(OrderSummaryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long saveOrderFromCart(Long customerId, OrderCommand.CreateFromCart command) {
        // 1. 고객 및 배달 지원 여부 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. Redis에서 해당 유저의 장바구니 조회
        RedisCart redisCart = cartService.getCart(customerId);
        if (redisCart == null || redisCart.getItems() == null || redisCart.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 3. 장바구니에 담긴 가게 ID로 가게 조회 및 검증
        Shop shop = shopRepository.findById(redisCart.getShopId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_NOT_FOUND));

        shop.validateSupport(command.getDeliveryType());

        // 4. 메뉴 조회 및 실시간 가격 변조 검증
        // Redis에 담긴 메뉴 ID들을 모아서 한 방에 조회 (In-Query)
        List<Long> menuIds = redisCart.getItems().stream()
                .map(RedisCartItem::getMenuId)
                .toList();
        List<Menu> menus = menuRepository.findAllById(menuIds);

        // 🚨 [중요 버그 수정] Redis에 담긴 메뉴 개수와 RDB에서 조회된 메뉴 개수가 다르면 예외를 던져야 합니다.
        // 그렇지 않으면 아래 6번 stream에서 RDB에 없는 메뉴인 경우 NullPointerException(500 에러)이 터집니다!
        if (menus.size() != redisCart.getItems().size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST); // "존재하지 않는 메뉴가 포함되어 있습니다."
        }

        // Redis에 저장된 장바구니 아이템들을 쉽게 매핑하기 위해 Map으로 변환
        Map<Long, RedisCartItem> cartItemMap = redisCart.getItems().stream()
                .collect(Collectors.toMap(RedisCartItem::getMenuId, item -> item));

        for (Menu menu : menus) {
            // (안전장치) RDB 최신 가게 ID와 맞는지 한 번 더 검증
            if (!menu.getShopId().equals(redisCart.getShopId())) {
                throw new BusinessException(ErrorCode.CART_SHOP_MISMATCH); // "해당 가게의 메뉴가 아닙니다."
            }

            // 장바구니에 담았던 시점의 가격과 RDB 최신 가격 대조 (위변조 방지)
            RedisCartItem cartItem = cartItemMap.get(menu.getId());
            if (menu.getPrice() != cartItem.getPrice()) {
                throw new BusinessException(ErrorCode.MENU_PRICE_CHANGED); // "메뉴의 가격이 변동되었습니다."
            }
        }

        // 5. 주소 및 배송 정보 결정
        Address address = resolveAddress2(command, customerId); // 필요시 고객 기본주소 등 활용
        Delivery delivery = Delivery.createDelivery(address, command.getDeliveryType());

        // 6. Redis 장바구니 데이터를 바탕으로 OrderItem 엔티티 생성
        List<OrderItem> orderItems = menus.stream()
                .map(m -> {
                    RedisCartItem cartItem = cartItemMap.get(m.getId());
                    return OrderItem.createOrderItem(m, m.getPrice(), cartItem.getCount());
                })
                .toList();

        // 7. 주문 생성 및 DB 저장
        Order order = Order.createOrder(shop, customer, delivery, command.getDeliveryType(), orderItems);
        orderRepository.save(order);

        // 8.주문이 성공적으로 저장되었으므로 Redis 장바구니를 시원하게 비워줍니다!
        cartService.clearCart(customerId);

        return order.getId();
    }

    @Transactional
    public Long saveOrder(OrderCommand.Create command) {
        //조회
        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("고객 정보를 찾을 수 없습니다."));

        Shop shop = shopRepository.findById(command.getShopId())
                .orElseThrow(() -> new EntityNotFoundException("가게 정보를 찾을 수 없습니다."));

        //배달 방식 지원 여부 검증
        shop.validateSupport(command.getDeliveryType());

        //메뉴 조회 및 수량 매칭
        Map<Long, Integer> counts = command.toMenuCountMap();
        List<Menu> menus = menuRepository.findAllById(counts.keySet());

        for (Menu menu: menus) {
            if (!menu.getShopId().equals(command.getShopId())) {
                throw new IllegalArgumentException("해당 가게의 메뉴가 아닌 상품이 포함되어 있습니다.");
            }
            //Menu 엔티티에 판매 상태(status) 필드가 있다면 추가로 검증 가능
        }

        //주소 결정
        Address address = resolveAddress(command);

        //배송정보 생성
        Delivery delivery = Delivery.createDelivery(address, command.getDeliveryType());

        //주문상품 생성
        List<OrderItem> orderItems = menus.stream()
                .map(m -> OrderItem.createOrderItem(m, m.getPrice(), counts.get(m.getId())))
                .toList();

        //주문 생성 & 저장
        Order order = Order.createOrder(shop, customer, delivery, command.getDeliveryType(), orderItems);
        orderRepository.save(order);

        return order.getId();
    }


    /**
     * 1. 주문 접수 (가게 사장님이 확인)
     */
    @Transactional
    public Order acceptOrder(Long orderId, int estimatedMinutes) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // Order: PENDING -> ACCEPTED (접수됨)
        order.accept();
        // Delivery: NONE -> PENDING (배차 대기 혹은 준비 대기)
        order.getDelivery().pending();

/*
        // 배달 주문인 경우에만 라이더 배차 요청 (조리 시간 전달)
        if (order.getDeliveryType() == DeliveryType.DREAM_DELIVERY) {
            riderService.requestRider(order, estimatedMinutes);
        }
*/
        orderRepository.save(order);
        return order;
    }

    /**
     * 2. 조리 시작
     */
    @Transactional
    public Order startCooking(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // Order: ACCEPTED -> PREPARING (조리 중)
        order.startCooking();
        // DeliveryStatus는 여전히 PENDING (상태 변화 없음)

        orderRepository.save(order);
        return order;
    }

    /**
     * 3. 조리 완료 (가장 중요한 분기점)
     */
    @Transactional
    public Order completeCooking(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // 1) OrderStatus는 그대로 PREPARING 유지 (픽업 전까진 조리중/배달대기임)
        // 2) Delivery: PENDING -> READY_FOR_PICKUP (준비 완료)
        order.getDelivery().ready();

        // 3) 타입별 후속 조치
/*
        if (order.getDeliveryType() == DeliveryType.TAKEOUT) {
            // 포장 고객에게 "와서 가져가세요" 알림
            notificationService.sendPickupMessage(order.getCustomer());
        } else {
            // 라이더에게 "음식 나왔으니 가져가세요" 신호
            riderService.notifyReady(order);
        }
*/
        orderRepository.save(order);
        return order;
    }

    /**
     * 4. 픽업 완료 (라이더 혹은 포장 고객이 수령)
     */
    @Transactional
    public Order pickupOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // 1) Order: PREPARING -> SHIPPING (배송 중)
        order.startDelivering();
        // 2) Delivery: READY_FOR_PICKUP -> DELIVERING (이동 중)
        order.getDelivery().delivering();

        orderRepository.save(order);
        return order;
    }

    /**
     * 5. 최종 완료 (배달 완료 혹은 포장 수령 완료)
     */
    @Transactional
    public Order completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        // 1) Delivery: SHIPPING -> DELIVERED (전달 완료)
        order.getDelivery().complete();
        // 2) Order: SHIPPING -> COMPLETED (최종 완료 - 정산 대상)
        order.complete();

//        notificationService.sendFinalCompletionMessage(order.getCustomer());

        orderRepository.save(order);
        return order;
    }


    /**
     * 조회 로직
     */
    //전체 조회
    public Page<OrderResponseDto> findAllOrders(Long customerId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByCustomerId(customerId, pageable);

        return orderPage.map(OrderResponseDto::of);
    }

    //상세 조회
    public OrderDetailResponseDto findOrderDetail(Long orderId, Long customerId) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId).orElseThrow();

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        return OrderDetailResponseDto.of(order);
    }

    //진행중인 주문 조회
    public List<OrderResponseDto> findActiveOrders(Long customerId) {
        List<Order> activeOrders = orderRepository.findActiveOrders(customerId);

        return activeOrders.stream()
                .map(OrderResponseDto::of)
                .collect(Collectors.toList());
    }



    /** 주소 추출 로직 */
    private Address resolveAddress(OrderCommand.Create command) {

        // 포장이면 주소 추출 없이 바로 null 반환 (또는 매장 주소 반환)
        if (command.getDeliveryType().isTakeout()) {
            return null;
        }

        // 배달인 경우에만 주소를 찾음
        //1. myAddressId가 있는 경우 DB 조회
        if (command.getMyAddressId() != null) {
            Optional<Address> address = myAddressRepository.findById(command.getMyAddressId())
                    .map(MyAddress::getAddress);
            if (address.isPresent()) {
                return address.get();
            }
        }

        //2. 위에서 주소를 못 찾고, 직접 입력 주소가 있는 경우
        if (command.getDirectAddress() != null) {
            return command.getDirectAddress();
        }

        //3. 모든 시도가 실패하면 예외 발생
        throw new IllegalArgumentException("배송지 주소는 필수입니다.");
    }


    private Address resolveAddress2(OrderCommand.CreateFromCart command, Long customerId) {
        // 1. 포장이면 주소 추출 없이 바로 null 반환
        if (command.getDeliveryType().isTakeout()) {
            return null;
        }

        // 2. 주소록 ID(myAddressId)가 넘어온 경우
        // 배달인 경우에만 주소를 찾음
        if (command.getMyAddressId() != null) {
            MyAddress myAddress = myAddressRepository.findByIdAndCustomerId(command.getMyAddressId(), customerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));
                    //"INVALID_ADDRESS_ACCESS" 같은 에러 코드를 만들어 써도 됨

            return myAddress.getAddress();
        }

        // 3. 직접 입력 주소(directAddress)가 넘어온 경우
        if (command.getDirectAddress() != null) {
            //만약 directAddress 내부 필드(city, street)가 비어있는지 검증이 필요하다면 처리
            return command.getDirectAddress();
        }

        // 4. 배달인데 두 주소 정보가 모두 없는 경우
        throw new BusinessException(ErrorCode.BAD_REQUEST); // "배송지 주소는 필수입니다."
    }
}
