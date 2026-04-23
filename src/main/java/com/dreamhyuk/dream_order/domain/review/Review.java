package com.dreamhyuk.dream_order.domain.review;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.order.Order;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Double score; //별점 (0.5 단위 제약 필요)
    private String content; //리뷰 내용

    @Builder
    private Review(Shop shop, Customer customer, Order order, Double score, String content) {
        //별점은 0.5점 단위여야 한다
        validateScore(score);

        this.shop = shop;
        this.customer = customer;
        this.order = order;
        this.score = score;
        this.content = content;
    }

    public static Review createReview(Shop shop, Customer customer, Order order, Double score, String content) {
        return Review.builder()
                .shop(shop)
                .customer(customer)
                .order(order)
                .score(score)
                .content(content)
                .build();
    }

    //== 비즈니스 로직 ==//
    /**
     * 생각해 봐야할 점!
     * 자바의 Double은 부동 소수점 방식이라 가끔 4.5가 4.499999999처럼 저장될 때가 있다.
     * 엄격한 시스템에선 이 문제를 피하기 위해 아래와 같은 방법을 쓰기도 한다.
     * --------------------------------------------
     * 1. 정수형(Integer) 처리: DB와 엔티티에는 0 ~ 50 사이의 정수를 저장하고(score * 10),
     *    화면에 보여줄 땐 10.0으로 나누어서 보여준다.
     * 2. BigDecimal 사용: 소수점 연산 오차를 원천 차단한다. 하지만 별점은 연산이 복잡하지 않아 Double로도 충분하다.
     */
    public void validateScore(Double score) {
        //1. null 체크
        if (score == null) {
            throw new IllegalArgumentException("별점을 입력하세요.");
        }

        //2. 범위 체크 (0.0 ~ 5.0)
        if (score < 0.0 || score > 5.0) {
            throw new IllegalArgumentException("별점은 0점 이상 5점 이하여야 합니다.");
        }

        //3. 0.5 단위 체크
        //score에 2를 곱한 결과가 소수점이 없는 정수여야 한다
        if ((score * 2) % 1.0 != 0) {
            throw new IllegalArgumentException("별점은 0.5점 단위만 입력 가능합니다.");
        }
    }
}
