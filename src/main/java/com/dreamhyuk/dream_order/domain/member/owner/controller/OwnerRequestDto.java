package com.dreamhyuk.dream_order.domain.member.owner.controller;

import com.dreamhyuk.dream_order.domain.member.owner.service.OwnerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class OwnerRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUp {

        @NotBlank(message = "이메일은 필수입니다.")
//        @Email
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "사업자 번호는 필수입니다.")
        private String businessNumber;

        public OwnerCommand.SignUp toCommand() {
            return OwnerCommand.SignUp.builder()
                    .email(this.email)
                    .password(this.password)
                    .businessNumber(this.businessNumber)
                    .build();
        }

    }

}
