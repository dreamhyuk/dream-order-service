package com.dreamhyuk.dream_order.global.init;

import com.dreamhyuk.dream_order.domain.category.Category;
import com.dreamhyuk.dream_order.domain.category.CategoryRepository;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import com.dreamhyuk.dream_order.domain.menu.Menu;
import com.dreamhyuk.dream_order.domain.menu.MenuGroup;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuGroupRepository;
import com.dreamhyuk.dream_order.domain.menu.repository.MenuRepository;
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
    private final MenuGroupRepository menuGroupRepository;
    private final MenuRepository menuRepository;
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
            //owner 조회
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
            shopRepository.save(shop1);

            MenuGroup group1 = MenuGroup.createMenuGroup("후라이드", 1, shop1.getId());
            MenuGroup group2 = MenuGroup.createMenuGroup("양념", 2, shop1.getId());
            MenuGroup group3 = MenuGroup.createMenuGroup("음료", 3, shop1.getId());
            menuGroupRepository.save(group1);
            menuGroupRepository.save(group2);
            menuGroupRepository.save(group3);

            Menu menu1 = Menu.createMenu("후라이드치킨", 20000, shop1.getId(), group1);
            Menu menu2 = Menu.createMenu("양념치킨", 21000, shop1.getId(), group2);
            Menu menu3 = Menu.createMenu("코카콜라", 3000, shop1.getId(), group3);
            group1.addMenu(menu1);
            group2.addMenu(menu2);
            group3.addMenu(menu3);
            menuRepository.save(menu1);
            menuRepository.save(menu2);
            menuRepository.save(menu3);

            ////////////////////////////////////////////////////////////////
            Shop shop2 = Shop.createShop(
                    owner2,
                    List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.SHOP_DELIVERY, DeliveryType.TAKEOUT),
                    List.of(chicken, fastFood),
                    address2,
                    "교촌치킨");
            shopRepository.save(shop2);

            MenuGroup group4 = MenuGroup.createMenuGroup("인기메뉴", 1, shop2.getId());
            MenuGroup group5 = MenuGroup.createMenuGroup("치킨", 2, shop2.getId());
            MenuGroup group6 = MenuGroup.createMenuGroup("음료", 3, shop2.getId());
            menuGroupRepository.save(group4);
            menuGroupRepository.save(group5);
            menuGroupRepository.save(group6);

            Menu menu4 = Menu.createMenu("허니콤보", 23000, shop2.getId(), group4);
            Menu menu5 = Menu.createMenu("레드콤보", 24000, shop2.getId(), group5);
            Menu menu6 = Menu.createMenu("사이다", 3000, shop2.getId(), group6);
            group4.addMenu(menu4);
            group5.addMenu(menu5);
            group6.addMenu(menu6);
            menuRepository.save(menu4);
            menuRepository.save(menu5);
            menuRepository.save(menu6);

            ////////////////////////////////////
            Shop shop3 = Shop.createShop(
                    owner3,
                    List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.DREAM_DELIVERY, DeliveryType.TAKEOUT),
                    List.of(pizza, fastFood),
                    address3,
                    "피자헛");
            shopRepository.save(shop3);

            MenuGroup group7 = MenuGroup.createMenuGroup("인기메뉴", 1, shop3.getId());
            MenuGroup group8 = MenuGroup.createMenuGroup("피자", 2, shop3.getId());
            MenuGroup group9 = MenuGroup.createMenuGroup("음료", 3, shop3.getId());
            menuGroupRepository.save(group7);
            menuGroupRepository.save(group8);
            menuGroupRepository.save(group9);

            Menu menu7 = Menu.createMenu("콤비네이션", 23000, shop3.getId(), group7);
            Menu menu8 = Menu.createMenu("페페로니", 24000, shop3.getId(), group8);
            Menu menu9 = Menu.createMenu("콜라", 3000, shop3.getId(), group9);
            group7.addMenu(menu7);
            group8.addMenu(menu8);
            group9.addMenu(menu9);
            menuRepository.save(menu7);
            menuRepository.save(menu8);
            menuRepository.save(menu9);

            elasticsearchOperations.save(ShopDocument.from(shop1, List.of(group1, group2, group3)));
            elasticsearchOperations.save(ShopDocument.from(shop2, List.of(group4, group5, group6)));
            elasticsearchOperations.save(ShopDocument.from(shop3, List.of(group7, group8, group9)));

            System.out.println("ES에 상점 데이터 동기화 완료!");
        }
    }
}

