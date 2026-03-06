package com.dreamhyuk.dream_order.domain.member.owner;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "owners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Owner {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "owner")
    private List<Shop> shops = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private String email;
    private String password;
    private String businessNumber;
}
