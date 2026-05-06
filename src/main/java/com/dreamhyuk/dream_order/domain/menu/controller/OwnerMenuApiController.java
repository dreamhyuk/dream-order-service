package com.dreamhyuk.dream_order.domain.menu.controller;

import com.dreamhyuk.dream_order.domain.menu.dto.MenuCreateRequestDto;
import com.dreamhyuk.dream_order.domain.menu.dto.MenuGroupRequestDto;
import com.dreamhyuk.dream_order.domain.menu.dto.MenuGroupUpdateRequestDto;
import com.dreamhyuk.dream_order.domain.menu.dto.MenuUpdateRequestDto;
import com.dreamhyuk.dream_order.domain.menu.service.MenuService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/shops/{shopId}")
public class OwnerMenuApiController {

    private final MenuService menuService;

    // --- MenuGroup API ---
    @PostMapping("/menu-groups")
    public ResponseEntity<Long> createMenuGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @RequestBody @Valid MenuGroupRequestDto request) throws AccessDeniedException {

        Long menuGroupId = menuService.saveGroup(userDetails.getMemberId(), shopId, request);

        return ResponseEntity.ok(menuGroupId);
    }

    @PatchMapping("/menu-groups/{groupId}")
    public ResponseEntity<Void> updateMenuGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long groupId,
            @RequestBody MenuGroupUpdateRequestDto request) throws AccessDeniedException {

        menuService.updateGroup(userDetails.getMemberId(), shopId, groupId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/menu-groups/{groupId}")
    public ResponseEntity<Void> deleteMenuGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long groupId) throws AccessDeniedException {

        menuService.deleteGroup(userDetails.getMemberId(), shopId, groupId);

        return ResponseEntity.noContent().build();
    }


    // --- Menu API ---
    /**
     * 메뉴를 생성할 땐 무조건 메뉴그룹에 속해야 하므로 groupId를 URL경로에 넣는다
     * 수정/삭제 시에는 이미 생성된 메뉴이므로 menuId만으로 식별 가능, 경로 단축을 위해 groupId는 생략
     */
    @PostMapping("/menu-groups/{groupId}/menus")
    public ResponseEntity<Long> createMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long groupId,
            @RequestBody MenuCreateRequestDto request) throws Exception {

        Long id = menuService.saveMenu(userDetails.getMemberId(), shopId, groupId, request);

        return ResponseEntity.ok(id);
    }

    @PatchMapping("/menus/{menuId}")
    public ResponseEntity<Void> updateMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long menuId,
            @RequestBody MenuUpdateRequestDto request) throws AccessDeniedException {

        menuService.updateMenu(userDetails.getMemberId(), shopId, menuId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long menuId) throws AccessDeniedException {

        menuService.deleteMenu(userDetails.getMemberId(), shopId, menuId);

        return ResponseEntity.noContent().build();
    }
}
