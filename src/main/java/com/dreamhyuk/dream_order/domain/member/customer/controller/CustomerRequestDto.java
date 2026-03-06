package com.dreamhyuk.dream_order.domain.member.customer.controller;

import com.dreamhyuk.dream_order.domain.member.customer.service.CustomerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CustomerRequestDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUp {
        @NotBlank(message = "이메일은 필수입니다.")
//        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
        private String password;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String username;

        //Controller에서 Service로 넘길 때 변환
        public CustomerCommand.SingUp toCommand() {
            return CustomerCommand.SingUp.builder()
                    .email(this.email)
                    .password(this.password)
                    .username(this.username)
                    .build();
        }
    }
}
