package com.dreamhyuk.dream_order.domain.member.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyAddressRepository extends JpaRepository<MyAddress, Long> {

    //전체 조회
    List<MyAddress> findAllByCustomerId(Long customerId);

    //단건 조회
    Optional<MyAddress> findByIdAndCustomerId(Long id, Long customerId);

}
