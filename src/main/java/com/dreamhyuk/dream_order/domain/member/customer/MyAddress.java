package com.dreamhyuk.dream_order.domain.member.customer;

import com.dreamhyuk.dream_order.domain.common.Address;
import jakarta.persistence.*;
import lombok.Getter;

import static jakarta.persistence.FetchType.*;

@Entity
@Table(name = "my_addresses")
@Getter
public class MyAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "my_address_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Embedded
    private Address address;

    private String nickname; // 우리집, 회사 등
}
