package com.dreamhyuk.dream_order.domain.menu.dto;

public record MenuDetailResponseDto(
        Long menuId,
        String menuName,
        int price,
        Long menuGroupId,
        String menuGroupName
//        String description, //메뉴 설명
//        String imageUrl //메뉴 이미지
) {}
