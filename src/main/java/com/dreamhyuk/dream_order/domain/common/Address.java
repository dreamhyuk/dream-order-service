package com.dreamhyuk.dream_order.domain.common;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address {

    private String city;
    private String street;
    private String zipcode;

    public static Address of(String city, String street, String zipcode) {
        return Address.builder()
                .city(city)
                .street(street)
                .zipcode(zipcode)
                .build();
    }
}
