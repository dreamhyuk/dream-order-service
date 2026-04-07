package com.dreamhyuk.dream_order.domain.menu.repository;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuGroupRepository extends JpaRepository<MenuGroup, Long> {

    //"SELECT 1 FROM menu_group WHERE shop_id = ? AND name = ?"
    boolean existsByShopIdAndName(Long shopId, String name);

    // shopId로 조회하되, 연관된 menus 리스트를 한 번에 가져옴 (N+1 방지)
    @EntityGraph(attributePaths = {"menus"})
    List<MenuGroup> findByShopId(Long shopId);
}
