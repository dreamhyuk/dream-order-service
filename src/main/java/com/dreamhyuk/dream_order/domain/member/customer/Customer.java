package com.dreamhyuk.dream_order.domain.member.customer;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Customer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "customer")
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<MyAddress> addresses = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MemberRole role = MemberRole.CUSTOMER;

    private String email;
    private String password;
    private String username;


}
