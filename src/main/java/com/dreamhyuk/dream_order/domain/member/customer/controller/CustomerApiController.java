package com.dreamhyuk.dream_order.domain.member.customer.controller;

import com.dreamhyuk.dream_order.domain.member.customer.service.CustomerCommand;
import com.dreamhyuk.dream_order.domain.member.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerApiController {

    private final CustomerService customerService;

    /** 회원 가입 */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@Valid @RequestBody CustomerRequestDto.SignUp request) {
        CustomerCommand.SingUp command = request.toCommand();

        Long id = customerService.saveCustomer(command);

        return ResponseEntity.ok(id);
    }

    /** 조회 */
//    @GetMapping("/me")
//    public String status()
}
