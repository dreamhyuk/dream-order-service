package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddress;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddressRepository;
import com.dreamhyuk.dream_order.domain.menu.Menu;
import com.dreamhyuk.dream_order.domain.menu.MenuRepository;
import com.dreamhyuk.dream_order.domain.delivery.Delivery;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderItem;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;
    private final MyAddressRepository myAddressRepository;

    @Transactional
    public Long order(OrderCommand.Create command) {
        //조회
        Customer customer = customerRepository.findById(command.getCustomerId()).orElseThrow();
        Shop shop = shopRepository.findById(command.getShopId()).orElseThrow();

        //배달 방식 지원 여부 검증
        shop.validateSupport(command.getDeliveryType());

        //주소 결정
        Address address = resolveAddress(command);

        //메뉴 조회 및 수량 매칭
        Map<Long, Integer> counts = command.toMenuCountMap();
        List<Menu> menus = menuRepository.findAllById(counts.keySet());

        //배송정보 생성
        Delivery delivery = Delivery.createDelivery(address, command.getDeliveryType());

        //주문상품 생성
        List<OrderItem> orderItems = menus.stream()
                .map(m -> OrderItem.createOrderItem(m, m.getPrice(), counts.get(m.getId())))
                .toList();

        //주문 생성
        Order order = Order.createOrder(shop, customer, delivery, command.getDeliveryType(), orderItems);

        //주문 저장
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




    //주소 추출 로직
    private Address resolveAddress(OrderCommand.Create command) {

        /** 포장이면 주소 추출 없이 바로 null 반환 (또는 매장 주소 반환) */
        if (command.getDeliveryType().isTakeout()) {
            return null;
        }

        /** 배달인 경우에만 주소를 찾음 */
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
}
