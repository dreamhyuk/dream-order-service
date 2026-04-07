package com.dreamhyuk.dream_order.domain.menu.dto;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuGroupResponseDto {

    private Long menuGroupId;
    private String name;
    private int priority;
    private List<MenuSummaryResponseDto> menus;

    public static MenuGroupResponseDto from(MenuGroup menuGroup) {
        return MenuGroupResponseDto.builder()
                .menuGroupId(menuGroup.getId())
                .name(menuGroup.getName())
                .priority(menuGroup.getPriority())
                .menus(menuGroup.getMenus().stream()
                        .map(MenuSummaryResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
