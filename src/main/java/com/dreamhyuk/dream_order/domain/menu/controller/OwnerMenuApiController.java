package com.dreamhyuk.dream_order.domain.menu.controller;

import com.dreamhyuk.dream_order.domain.menu.dto.MenuCreateRequestDto;
import com.dreamhyuk.dream_order.domain.menu.dto.MenuGroupRequestDto;
import com.dreamhyuk.dream_order.domain.menu.dto.MenuGroupUpdateRequestDto;
import com.dreamhyuk.dream_order.domain.menu.service.MenuService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/shops/{shopId}/menu-groups")
public class OwnerMenuApiController {

    private final MenuService menuService;

    // --- MenuGroup API ---
    @PostMapping
    public ResponseEntity<Long> createMenuGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @RequestBody @Valid MenuGroupRequestDto request) throws AccessDeniedException {

        Long menuGroupId = menuService.saveGroup(userDetails.getMemberId(), shopId, request);

        return ResponseEntity.ok(menuGroupId);
    }

    @PatchMapping("/{menuGroupId}")
    public ResponseEntity<Void> updateMenuGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long menuGroupId,
            @RequestBody MenuGroupUpdateRequestDto request) throws AccessDeniedException {

        menuService.updateGroup(userDetails.getMemberId(), shopId, menuGroupId, request);

        return ResponseEntity.ok().build();
    }


    // --- Menu API ---
    @PostMapping("/{menuGroupId}/menus")
    public ResponseEntity<Long> createMenu(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @PathVariable Long menuGroupId,
            @RequestBody MenuCreateRequestDto request) throws Exception {

        Long id = menuService.saveMenu(userDetails.getMemberId(), shopId, menuGroupId, request);

        return ResponseEntity.ok(id);
    }
}
