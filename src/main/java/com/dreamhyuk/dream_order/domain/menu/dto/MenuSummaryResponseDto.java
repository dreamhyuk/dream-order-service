package com.dreamhyuk.dream_order.domain.menu.dto;

import com.dreamhyuk.dream_order.domain.menu.Menu;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuSummaryResponseDto {

    private Long id;
    private String menuName;
    private int price;

    public static MenuSummaryResponseDto from(Menu menu) {
        return MenuSummaryResponseDto.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .price(menu.getPrice())
                .build();
    }
}
