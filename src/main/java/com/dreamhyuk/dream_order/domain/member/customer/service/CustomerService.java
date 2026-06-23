package com.dreamhyuk.dream_order.domain.member.customer.service;

import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddress;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddressRepository;
import com.dreamhyuk.dream_order.domain.member.customer.dto.CustomerResponseDto;
import com.dreamhyuk.dream_order.domain.member.customer.dto.MyAddressResponse;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final MyAddressRepository myAddressRepository;
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

    /**
     * 신규 주소록 등록
     */
    @Transactional
    public Long saveAddress(Long customerId, AddressCommand.Register command) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 커맨드 내부 메서드를 통해 엔티티 조립 후 저장
        MyAddress myAddress = command.toEntity(customer);
        myAddressRepository.save(myAddress);

        return myAddress.getId();
    }

    /**
     * 내 주소록 전체 조회
     */
    public List<MyAddressResponse> getMyAddresses(Long customerId) {
        // 특정 고객의 주소록만 안전하게 긁어와서 DTO 리스트로 변환
        return myAddressRepository.findAllByCustomerId(customerId).stream()
                .map(MyAddressResponse::from)
                .toList();
    }

}
