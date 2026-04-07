package com.dreamhyuk.dream_order.domain.menu.service;

import com.dreamhyuk.dream_order.domain.menu.Menu;
import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import com.dreamhyuk.dream_order.domain.menu.dto.*;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuGroupRepository;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuGroupRepositoryCustom;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuRepository;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final ShopRepository shopRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MenuGroupRepositoryCustom menuGroupRepositoryCustom;

    // --- Menu Group 관련 로직 ---
    @Transactional
    public Long saveGroup(Long ownerId, Long shopId, MenuGroupRequestDto request) throws AccessDeniedException {
        //소유권 검증 (가게 사장님이 맞는가?)
        validateShopOwner(shopId, ownerId);

        if (menuGroupRepository.existsByShopIdAndName(shopId, request.getName())) {
            throw new IllegalStateException("이미 존재하는 메뉴그룹입니다.");
        }

        MenuGroup menuGroup = request.toEntity(shopId);

        menuGroupRepository.save(menuGroup);

        return menuGroup.getId();
    }

    @Transactional
    public void updateGroup(Long ownerId, Long shopId, Long menuGroupId, MenuGroupUpdateRequestDto request) throws AccessDeniedException {
        //소유권 검증 (가게 사장님이 맞는가?)
        validateShopOwner(shopId, ownerId);
        //소속 검증
        MenuGroup menuGroup = validateMenuGroupInShop(menuGroupId, shopId);

        menuGroup.update(request.getName(), request.getPriority());
    }


    // --- Menu 관련 로직 ---
    @Transactional
    public Long saveMenu(Long ownerId, Long shopId, Long menuGroupId, MenuCreateRequestDto request) throws Exception {
        //소유권 검증 (가게 사장님이 맞는가?)
        validateShopOwner(shopId, ownerId);
        //그룹 검증 (해당 메뉴그룹이 이 가게 소속인가?)
        MenuGroup menuGroup = validateMenuGroupInShop(menuGroupId, shopId);

        Menu menu = Menu.createMenu(
                request.getMenuName(),
                request.getPrice(),
                shopId,
                menuGroup
        );

        menuRepository.save(menu);

        return menu.getId();
    }

    /** 검증 로직 */
    //가게 소유권 검증
    private void validateShopOwner(Long shopId, Long ownerId) throws AccessDeniedException {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("가게를 찾을 수 없습니다. ID: " + shopId));

        // Shop 엔티티에 저장된 ownerId와 현재 로그인한 ownerId를 비교
        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("해당 가게에 대한 관리 권한이 없습니다.");
        }
    }

    //메뉴그룹 소속 검증
    private MenuGroup validateMenuGroupInShop(Long menuGroupId, Long shopId) {
        MenuGroup menuGroup = menuGroupRepository.findById(menuGroupId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 메뉴 그룹입니다. ID: " + menuGroupId));

        if (!menuGroup.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("해당 가게에 속하지 않은 메뉴 그룹입니다.");
        }

        return menuGroup;
    }


    /** 메뉴 조회 (Customer) */
    //해당 가게 전체 메뉴 조회
    public List<MenuGroupResponseDto> getMenus(Long shopId) {
        //1. 해당 가게의 모든 메뉴그룹 조회 (Menu와 fetch join)
        List<MenuGroup> menuGroups = menuGroupRepositoryCustom.findByShopIdOrderByPriority(shopId);

        //2. 엔티티를 dto로 변환해서 계층 구조 생성
        List<MenuGroupResponseDto> menuGroupResponses = menuGroups.stream()
                .map(MenuGroupResponseDto::from)
                .toList();

        return menuGroupResponses;
    }

    //단건 조회
    public MenuDetailResponseDto getMenuDetail(Long shopId, Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("메뉴를 찾을 수 없습니다."));

        if (!menu.getShopId().equals(shopId)) {
            throw new IllegalArgumentException("해당 가게의 메뉴가 아닙니다.");
        }

        return new MenuDetailResponseDto(
                menu.getId(),
                menu.getMenuName(),
                menu.getPrice(),
                menu.getMenuGroup().getId(),
                menu.getMenuGroup().getName()
        );
    }
}
