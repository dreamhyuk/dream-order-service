package com.dreamhyuk.dream_order.domain.member.owner.service;

import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long saveOwner(OwnerCommand.SignUp command) {

        String encodedPassword = passwordEncoder.encode(command.getPassword());
        Owner owner = command.toEntity(encodedPassword);

        ownerRepository.save(owner);

        return owner.getId();
    }
}
