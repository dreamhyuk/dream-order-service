package com.dreamhyuk.dream_order.domain.cart.service;

import com.dreamhyuk.dream_order.domain.cart.MartCart;
import com.dreamhyuk.dream_order.domain.cart.MartCartItem;
import com.dreamhyuk.dream_order.domain.product.Product;
import com.dreamhyuk.dream_order.domain.product.repository.ProductRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 마트 장바구니. 음식 장바구니(cart:{customerId})와 별도 키(mart-cart:{customerId})에 저장한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MartCartService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    /** 장바구니에 상품을 담고 Redis에 저장 */
    public void addProduct(Long customerId, Long productId, int count) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 판매 중지된 상품은 담을 수 없다
        if (!product.isOnSale()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        MartCart cart = getCart(customerId);
        cart.addCartItem(new MartCartItem(product.getId(), product.getName(), product.getPrice(), count));

        save(customerId, cart);
    }

    /** MartOrderService 등 외부에서 필요할 때 장바구니 객체를 꺼내주는 메서드 */
    public MartCart getCart(Long customerId) {
        String json = redisTemplate.opsForValue().get(key(customerId));
        if (json == null) {
            return MartCart.createEmptyCart(String.valueOf(customerId));
        }

        try {
            return objectMapper.readValue(json, MartCart.class);
        } catch (Exception e) {
            throw new RuntimeException("Mart cart parsing failed", e);
        }
    }

    /** 상품 하나 삭제 */
    public void removeProduct(Long customerId, Long productId) {
        MartCart cart = getCart(customerId);
        cart.removeCartItem(productId);

        if (cart.getItems().isEmpty()) {
            redisTemplate.delete(key(customerId));
        } else {
            save(customerId, cart);
        }
    }

    /** 주문이 완료되면 장바구니 clear */
    public void clearCart(Long customerId) {
        redisTemplate.delete(key(customerId));
    }

    private void save(Long customerId, MartCart cart) {
        try {
            String json = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(key(customerId), json, Duration.ofDays(3));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save mart cart to Redis", e);
        }
    }

    private String key(Long customerId) {
        return "mart-cart:" + customerId;
    }
}
