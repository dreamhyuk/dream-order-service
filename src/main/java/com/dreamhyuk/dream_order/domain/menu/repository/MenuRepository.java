package com.dreamhyuk.dream_order.domain.menu.repository;

import com.dreamhyuk.dream_order.domain.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 메뉴 ID로 조회하여 해당 메뉴가 속한 shopId만 반환
    // select m.shopId from Menu m where m.id = :id
    @Query("SELECT m.shopId FROM Menu m WHERE m.id = :id")
    Optional<Long> findShopIdByMenuId(@Param("id") Long id);

}
