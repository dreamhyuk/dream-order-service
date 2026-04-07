package com.dreamhyuk.dream_order.domain.menu.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShopMenuResponseDto {

    private Long shopId;
    private List<MenuGroupResponseDto> menuGroups;
}
