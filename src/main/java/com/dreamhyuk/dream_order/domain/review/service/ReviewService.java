package com.dreamhyuk.dream_order.domain.review.service;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
import com.dreamhyuk.dream_order.domain.order.OrderType;
import com.dreamhyuk.dream_order.domain.review.Review;
import com.dreamhyuk.dream_order.domain.review.repository.ReviewRepository;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public Long saveReview(ReviewCommand.Create command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer Not Found"));
        Order order = orderRepository.findById(command.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order Not Found"));

        //리뷰는 가게에 남기는 것이므로 음식 주문에만 쓸 수 있다
        if (order.getOrderType() != OrderType.FOOD) {
            throw new IllegalArgumentException("음식 주문에만 리뷰를 작성할 수 있습니다.");
        }

        //주문은 가게를 ID로만 참조하므로 가게를 따로 조회한다
        Shop shop = shopRepository.findById(order.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Shop Not Found"));

        Review review = Review.createReview(shop, customer, order, command.getScore(), command.getContent());

        reviewRepository.save(review);

        //Shop 별점 업데이트
        shop.updateRating(review.getScore());

        return review.getId();
    }

    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("customer not found"));

        reviewRepository.deleteById(reviewId);
    }
}
