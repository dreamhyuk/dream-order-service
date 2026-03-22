package com.dreamhyuk.dream_order.global.init;

import com.dreamhyuk.dream_order.domain.category.Category;
import com.dreamhyuk.dream_order.domain.category.CategoryRepository;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopDocument;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    @Transactional
    public void run(String... args) {
        String commonPassword1 = passwordEncoder.encode("1111");
        String commonPassword2 = passwordEncoder.encode("2222");
        String commonPassword3 = passwordEncoder.encode("3333");

        // 중복 생성 방지를 위한 체크 후 저장
        if (customerRepository.findByEmail("customer1@test.com").isEmpty()) {
            customerRepository.save(Customer.builder()
                    .email("customer1@test.com")
                    .password(commonPassword1)
                    .username("customer1")
                    .role(MemberRole.CUSTOMER)
                    .build());
        }
        if (customerRepository.findByEmail("customer2@test.com").isEmpty()) {
            customerRepository.save(Customer.builder()
                    .email("customer2@test.com")
                    .password(commonPassword2)
                    .username("customer2")
                    .role(MemberRole.CUSTOMER)
                    .build());
        }
        if (customerRepository.findByEmail("customer3@test.com").isEmpty()) {
            customerRepository.save(Customer.builder()
                    .email("customer3@test.com")
                    .password(commonPassword3)
                    .username("customer3")
                    .role(MemberRole.CUSTOMER)
                    .build());
        }


        if (ownerRepository.findByEmail("owner1@test.com").isEmpty()) {
            ownerRepository.save(Owner.builder()
                    .email("owner1@test.com")
                    .password(commonPassword1)
                    .businessNumber("1111111-11")
                    .role(MemberRole.OWNER)
                    .build());
        }
        if (ownerRepository.findByEmail("owner2@test.com").isEmpty()) {
            ownerRepository.save(Owner.builder()
                    .email("owner2@test.com")
                    .password(commonPassword2)
                    .businessNumber("22222-222")
                    .role(MemberRole.OWNER)
                    .build());
        }
        if (ownerRepository.findByEmail("owner3@test.com").isEmpty()) {
            ownerRepository.save(Owner.builder()
                    .email("owner3@test.com")
                    .password(commonPassword3)
                    .businessNumber("33-3-333-3")
                    .role(MemberRole.OWNER)
                    .build());
        }

        //카테고리 데이터
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("CHICKEN", "치킨"));
            categoryRepository.save(new Category("KOREAN", "한식"));
            categoryRepository.save(new Category("PIZZA", "피자"));
            categoryRepository.save(new Category("FAST_FOOD", "패스트푸드"));
            categoryRepository.save(new Category("CHINESE", "중식"));
            categoryRepository.save(new Category("CAFE", "카페.디저트"));
        }

        // 1. Shop 데이터 생성 로직 추가
        if (shopRepository.count() == 0) {
            // 이미 저장된 데이터 찾아오기
            Owner owner1 = ownerRepository.findByEmail("owner1@test.com")
                    .orElseThrow(() -> new RuntimeException("Owner1를 찾을 수 없습니다."));
            Owner owner2 = ownerRepository.findByEmail("owner2@test.com")
                    .orElseThrow(() -> new RuntimeException("Owner2를 찾을 수 없습니다."));
            Owner owner3 = ownerRepository.findByEmail("owner3@test.com")
                    .orElseThrow(() -> new RuntimeException("Owner3를 찾을 수 없습니다."));

            //카테고리
            Category chicken = categoryRepository.findByCategoryType("CHICKEN")
                    .orElseThrow();
            Category fastFood = categoryRepository.findByCategoryType("FAST_FOOD")
                    .orElseThrow();
            Category korean = categoryRepository.findByCategoryType("KOREAN")
                    .orElseThrow();
            Category pizza = categoryRepository.findByCategoryType("PIZZA")
                    .orElseThrow();

            Address address1 = Address.of("Seoul", "pungnab", "56789");
            Address address2 = Address.of("Seoul", "sungnae", "12345");
            Address address3 = Address.of("GWANG", "asdfg", "0990909");

            // 2. Shop 객체 생성 및 연관관계 매핑
            Shop shop1 = Shop.createShop(
                    owner1,
                    List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.TAKEOUT),
                    List.of(chicken, fastFood),
                    address1,
                    "네네치킨");
            Shop shop2 = Shop.createShop(
                    owner2,
                    List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.SHOP_DELIVERY, DeliveryType.TAKEOUT),
                    List.of(chicken, fastFood),
                    address2,
                    "교촌치킨");
            Shop shop3 = Shop.createShop(
                    owner3,
                    List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.DREAM_DELIVERY, DeliveryType.TAKEOUT),
                    List.of(pizza, fastFood),
                    address3,
                    "피자헛");


            shopRepository.save(shop1);
            shopRepository.save(shop2);
            shopRepository.save(shop3);
            elasticsearchOperations.save(ShopDocument.from(shop1));
            elasticsearchOperations.save(ShopDocument.from(shop2));
            elasticsearchOperations.save(ShopDocument.from(shop3));

            System.out.println("ES에 상점 데이터 동기화 완료!");
        }
    }
}

