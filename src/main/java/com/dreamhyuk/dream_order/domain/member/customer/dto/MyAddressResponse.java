package com.dreamhyuk.dream_order.domain.member.customer.dto;

import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.customer.MyAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyAddressResponse {

    private Long id;            //프론트가 myAddressId로 쏠 값
    private String addressName; //주소 별칭 (예: "우리집")
    private String city;
    private String street;
    private String zipcode;
    private String fullAddress; //화면에 한 줄로 보여주기 위한 가공 필드

    /**
     * 엔티티를 응답 DTO로 변환하는 정적 팩토리 메서드
     */
    public static MyAddressResponse from(MyAddress myAddress) {
        Address addr = myAddress.getAddress();
        String fullAddress = String.format("%s %s (%s)", addr.getCity(), addr.getStreet(), addr.getZipcode());

        return new MyAddressResponse(
                myAddress.getId(),
                myAddress.getAddressName(),
                addr.getCity(),
                addr.getStreet(),
                addr.getZipcode(),
                fullAddress
        );
    }
}
