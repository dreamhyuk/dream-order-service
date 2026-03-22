package com.dreamhyuk.dream_order.domain.shop.controller;

import com.dreamhyuk.dream_order.domain.shop.dto.ShopCreateRequestDto;
import com.dreamhyuk.dream_order.domain.shop.service.ShopCommand;
import com.dreamhyuk.dream_order.domain.shop.service.ShopService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
