package com.dreamhyuk.dream_order.domain.order.service;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddress;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddressRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 배달 주문의 배송지를 정한다. 음식 주문과 마트 주문이 같이 쓴다.
 */
@Component
@RequiredArgsConstructor
public class DeliveryAddressResolver {

    private final MyAddressRepository myAddressRepository;

    public Address resolve(Long customerId, Long myAddressId, Address directAddress) {
        // 1. 주소록 ID(myAddressId)가 넘어온 경우: 본인 주소록의 주소만 쓸 수 있다
        if (myAddressId != null) {
            MyAddress myAddress = myAddressRepository.findByIdAndCustomerId(myAddressId, customerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

            return myAddress.getAddress();
        }

        // 2. 직접 입력 주소(directAddress)가 넘어온 경우
        if (directAddress != null) {
            return directAddress;
        }

        // 3. 두 주소 정보가 모두 없는 경우
        throw new BusinessException(ErrorCode.BAD_REQUEST); // "배송지 주소는 필수입니다."
    }
}
