package com.dreamhyuk.dream_order.domain.member.owner.controller;

import com.dreamhyuk.dream_order.domain.member.owner.service.OwnerCommand;
import com.dreamhyuk.dream_order.domain.member.owner.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners")
public class OwnerApiController {

    private final OwnerService ownerService;

    /** 회원 가입 */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@Valid @RequestBody OwnerRequestDto.SignUp request) {
        OwnerCommand.SignUp command = request.toCommand();

        Long id = ownerService.saveOwner(command);

        return ResponseEntity.ok(id);
    }
}
