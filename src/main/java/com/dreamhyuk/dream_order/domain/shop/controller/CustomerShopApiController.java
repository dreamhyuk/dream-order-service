package com.dreamhyuk.dream_order.domain.shop.controller;

import com.dreamhyuk.dream_order.domain.menu.dto.MenuGroupResponseDto;
import com.dreamhyuk.dream_order.domain.menu.service.MenuService;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopSearchResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopSearchRequestDto;
import com.dreamhyuk.dream_order.domain.shop.service.ShopSearchCommand;
import com.dreamhyuk.dream_order.domain.shop.service.ShopService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/shops")
public class CustomerShopApiController {

    private final ShopService shopService;
    private final MenuService menuService;

    /**
     * 검색
     */
    @GetMapping
    public ResponseEntity<List<ShopSearchResponseDto>> getShops(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute ShopSearchRequestDto request) {

        ShopSearchCommand command = request.toCommand();

        List<ShopSearchResponseDto> results = shopService.search(command);

        return ResponseEntity.ok(results);
    }


    //가게 기본 상세 정보 조회
    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponseDto> getShopDetail(@PathVariable Long shopId) {
        ShopResponseDto shopDetail = shopService.getShopDetail(shopId);

        return ResponseEntity.ok(shopDetail);
    }

    //전체 메뉴 조회
    @GetMapping("/{shopId}/menus")
    public ResponseEntity<List<MenuGroupResponseDto>> getMenus(@PathVariable Long shopId) {

        List<MenuGroupResponseDto> responses = menuService.getMenus(shopId);

        return ResponseEntity.ok(responses);
    }

}
