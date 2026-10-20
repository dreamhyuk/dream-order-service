package com.dreamhyuk.dream_order.domain.shop.controller;

import com.dreamhyuk.dream_order.domain.shop.dto.OwnerShopDetailResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopCreateRequestDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopSummaryResponse;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopUpdateRequestDto;
import com.dreamhyuk.dream_order.domain.shop.service.ShopCommand;
import com.dreamhyuk.dream_order.domain.shop.service.ShopService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/shops")
public class OwnerShopApiController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<Long> createShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ShopCreateRequestDto request) {

        Long ownerId = userDetails.getMemberId();

        ShopCommand.Create command = request.toCommand(ownerId);
        Long shopId = shopService.saveShop(command);

        return ResponseEntity.ok(shopId);
    }

    @GetMapping
    public ResponseEntity<List<ShopSummaryResponse>> getMyShops(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ShopSummaryResponse> results = shopService.getShopsByOwner(userDetails.getMemberId());

        return ResponseEntity.ok(results);
    }

    //가게 상세 (수정 화면에서 현재 값을 채우는 용도)
    @GetMapping("/{shopId}")
    public ResponseEntity<OwnerShopDetailResponseDto> getMyShopDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId) throws AccessDeniedException {

        return ResponseEntity.ok(shopService.getOwnerShopDetail(shopId, userDetails.getMemberId()));
    }

    @PatchMapping("/{shopId}")
    public ResponseEntity<Void> updateShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId,
            @RequestBody ShopUpdateRequestDto request) throws AccessDeniedException {

        Long ownerId = userDetails.getMemberId();

        shopService.updateShop(shopId, ownerId, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<Void> deleteShop(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long shopId) throws AccessDeniedException {

        shopService.deleteShop(shopId, userDetails.getMemberId());

        return ResponseEntity.noContent().build();

    }
}
