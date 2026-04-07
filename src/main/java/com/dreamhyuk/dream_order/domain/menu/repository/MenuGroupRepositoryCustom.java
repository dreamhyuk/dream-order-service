package com.dreamhyuk.dream_order.domain.menu.repository;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;

import java.util.List;

public interface MenuGroupRepositoryCustom {

    List<MenuGroup> findByShopIdOrderByPriority(Long shopId);
}
