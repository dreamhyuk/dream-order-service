package com.dreamhyuk.dream_order.domain.menu.dto;

import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuGroupRequestDto {

    @NotBlank(message = "MenuGroup 이름은 필수입니다.")
    private String groupName;

    //값을 프론트에서 전달받자.
    @NotNull(message = "priority는 필수입니다.")
    private Integer priority;


    public MenuGroup toEntity(Long shopId) {
        return MenuGroup.createMenuGroup(this.groupName, this.priority, shopId);
    }
}
