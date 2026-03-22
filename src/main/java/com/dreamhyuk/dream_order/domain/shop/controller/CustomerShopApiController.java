package com.dreamhyuk.dream_order.domain.shop.controller;

import com.dreamhyuk.dream_order.domain.shop.dto.ShopSearchResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopSearchRequestDto;
import com.dreamhyuk.dream_order.domain.shop.service.ShopSearchCommand;
import com.dreamhyuk.dream_order.domain.shop.service.ShopService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/shops")
public class CustomerShopApiController {

    private final ShopService shopService;


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


}
