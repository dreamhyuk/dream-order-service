package com.dreamhyuk.dream_order.domain.menu.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuGroupUpdateRequestDto {

    private String groupName;

    private Integer priority;
}
