package com.dreamhyuk.dream_order.global.init;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String commonPassword = passwordEncoder.encode("1111");

        // 중복 생성 방지를 위한 체크 후 저장
        if (customerRepository.findByEmail("customer1@test.com").isEmpty()) {
            customerRepository.save(Customer.builder()
                    .email("customer1@test.com")
                    .password(commonPassword)
                    .username("customer1")
                    .role(MemberRole.CUSTOMER)
                    .build());
        }

        if (ownerRepository.findByEmail("owne1r@test.com").isEmpty()) {
            ownerRepository.save(Owner.builder()
                    .email("owner1@test.com")
                    .password(commonPassword)
                    .role(MemberRole.OWNER)
                    .build());
        }
    }
}
