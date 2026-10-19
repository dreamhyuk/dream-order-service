package com.dreamhyuk.dream_order.domain.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuSoldOutRequestDto {

    @NotNull(message = "품절 여부는 필수입니다.")
    private Boolean soldOut;

}
