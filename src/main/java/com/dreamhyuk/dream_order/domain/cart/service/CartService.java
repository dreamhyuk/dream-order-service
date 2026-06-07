package com.dreamhyuk.dream_order.domain.cart.service;

import com.dreamhyuk.dream_order.domain.cart.RedisCart;
import com.dreamhyuk.dream_order.domain.cart.RedisCartItem;
import com.dreamhyuk.dream_order.domain.menu.Menu;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final MenuRepository menuRepository;

    /** 장바구니에 메뉴를 추가하고 Redis에 저장 */
    @Transactional
    public void addMenu(Long customerId, Long shopId, Long menuId, int count) {
        String key = "cart:" + customerId;

        String json = redisTemplate.opsForValue().get(key);
        RedisCart cart;

        if (json != null) {
            //기존 카트가 있으면 객체로 복원 (역직렬화)
            try {
                cart = objectMapper.readValue(json, RedisCart.class);
            } catch (Exception e) {
                throw new RuntimeException("Cart parsing failed!", e);
            }
        } else {
            //기존 카트가 없으면 새로 만든다
            cart = RedisCart.createEmptyCart(String.valueOf(customerId));
        }

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("Menu Not Found"));

        RedisCartItem cartItem = new RedisCartItem(menu.getId(), menu.getMenuName(), menu.getPrice(), count);

        cart.addCartItem(cartItem, shopId);

        //변경된 장바구니 객체를 다시 json 문자열로 Rdis에 저장
        try {
            String updatedJson = objectMapper.writeValueAsString(cart);
            //저장하면서 3일의 유효기간(TTL)을 함께 설정
            redisTemplate.opsForValue().set(key, updatedJson, Duration.ofDays(3));
        } catch (Exception e) {
            throw new RuntimeException("Failed!!", e);
        }
    }

    /** OrderService 등 외부에서 필요할 때 장바구니 객체를 꺼내주는 메서드 */
    public RedisCart getCart(Long customerId) {
        String json = redisTemplate.opsForValue().get("cart:" + customerId);

        if (json == null) {
            return RedisCart.createEmptyCart(String.valueOf(customerId));
        }
        try {
            return objectMapper.readValue(json, RedisCart.class);
        } catch (Exception e) {
            throw new RuntimeException("GET Failed", e);
        }
    }

    /** 단일 메뉴 삭제 */
    public void removeMenu(Long customerId, Long menuId) {
        String key = "cart:" + customerId;

        // 1. Redis에서 기존 장바구니 가져오기
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        try {
            // 자바 객체로 복원
            RedisCart cart = objectMapper.readValue(json, RedisCart.class);

            // 2. 도메인 객체에게 특정 메뉴 삭제 명령 내리기
            cart.removeCartItem(menuId);

            // 3. 🌟 삭제 후 장바구니가 완전히 비었다면 아예 Redis Key를 날려버리는 게 깔끔합니다.
            if (cart.getItems().isEmpty()) {
                redisTemplate.delete(key);
            } else {
                // 아직 다른 메뉴가 남아있다면 변경된 상태를 다시 JSON으로 말아서 덮어쓰기
                String updatedJson = objectMapper.writeValueAsString(cart);
                redisTemplate.opsForValue().set(key, updatedJson, Duration.ofDays(3));
            }

        } catch (Exception e) {
            throw new RuntimeException("장바구니 메뉴 삭제 처리 실패", e);
        }

    }

    /** 주문이 완료되면 장바구니 clear */
    public void clearCart(Long customerId) {
        String key = "cart:" + customerId;
        redisTemplate.delete(key);
    }

}
