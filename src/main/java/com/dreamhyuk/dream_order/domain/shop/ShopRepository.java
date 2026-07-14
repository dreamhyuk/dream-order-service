package com.dreamhyuk.dream_order.domain.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {


    List<Shop> findByOwnerId(Long ownerId);
}
