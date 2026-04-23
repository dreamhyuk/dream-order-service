package com.dreamhyuk.dream_order.domain.review.service;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.order.OrderRepository;
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

    @Transactional
    public Long saveReview(ReviewCommand.Create command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer Not Found"));
        Order order = orderRepository.findWithShopById(command.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order Not Found"));

        Review review = Review.createReview(order.getShop(), customer, order, command.getScore(), command.getContent());

        reviewRepository.save(review);

        //Shop 별점 업데이트
        order.getShop().updateRating(review.getScore());

        return review.getId();
    }
}
