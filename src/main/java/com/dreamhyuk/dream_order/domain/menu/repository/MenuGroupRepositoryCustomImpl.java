package com.dreamhyuk.dream_order.domain.menu.repository;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.dreamhyuk.dream_order.domain.menu.QMenu.menu;
import static com.dreamhyuk.dream_order.domain.menu.QMenuGroup.menuGroup;

@Repository
@RequiredArgsConstructor
public class MenuGroupRepositoryCustomImpl implements MenuGroupRepositoryCustom{

    private final JPAQueryFactory query;

    @Override
    public List<MenuGroup> findByShopIdOrderByPriority(Long shopId) {
        return query
                .selectFrom(menuGroup)
                .leftJoin(menuGroup.menus, menu).fetchJoin()
                .where(menuGroup.shopId.eq(shopId))
                .orderBy(menuGroup.priority.asc(), menu.id.asc()) //그룹 순서 후 메뉴 ID순
                .fetch();
    }
}
