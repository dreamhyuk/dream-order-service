package com.dreamhyuk.dream_order.domain.member.customer.service;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.customer.controller.CustomerResponseDto;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long saveCustomer(CustomerCommand.SingUp command) {

        String encodedPassword = passwordEncoder.encode(command.getPassword());
        Customer customer = command.toEntity(encodedPassword);

        customerRepository.save(customer);

        return customer.getId();
    }

    public CustomerResponseDto.Profile findProfileById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return CustomerResponseDto.Profile.from(customer);
    }
}
