package com.dreamhyuk.dream_order.domain.menu.controller;

import com.dreamhyuk.dream_order.domain.menu.dto.MenuDetailResponseDto;
import com.dreamhyuk.dream_order.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerMenuApiController {

    private final MenuService menuService;

    @GetMapping("/shops/{shopId}/menus/{menuId}")
    public ResponseEntity<MenuDetailResponseDto> getMenuDetail(
            @PathVariable Long shopId,
            @PathVariable Long menuId) {

        MenuDetailResponseDto response = menuService.getMenuDetail(shopId, menuId);

        return ResponseEntity.ok(response);
    }

}
