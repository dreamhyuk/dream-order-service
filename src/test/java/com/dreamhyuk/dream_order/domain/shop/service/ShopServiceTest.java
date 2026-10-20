package com.dreamhyuk.dream_order.domain.shop.service;

import com.dreamhyuk.dream_order.domain.category.Category;
import com.dreamhyuk.dream_order.domain.category.CategoryRepository;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import com.dreamhyuk.dream_order.domain.order.DeliveryType;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopDocument;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import com.dreamhyuk.dream_order.domain.shop.dto.OwnerShopDetailResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopUpdateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ShopServiceTest {

    //카테고리 타입, 이메일이 테스트끼리 겹치지 않도록 매번 새 번호를 쓴다
    private static final AtomicInteger SEQ = new AtomicInteger();

    @MockBean
    private ElasticsearchOperations elasticsearchOperations; // 가짜 객체 주입

    @Autowired private ShopService shopService;
    @Autowired private ShopRepository shopRepository;
    @Autowired private OwnerRepository ownerRepository;
    @Autowired private CategoryRepository categoryRepository;

//    @MockBean
//    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void saveMockData() {
        // 1. 첫 번째 가게 (치킨 카테고리)
        ShopDocument shop1 = ShopDocument.builder()
                .id("1")
                .name("네네치킨 강남점")
                .categories(List.of(
                        new ShopDocument.CategoryInfo(1L, "CHICKEN", "치킨"),
                        new ShopDocument.CategoryInfo(4L, "FAST_FOOD", "패스트푸드")
                ))
                .build();

        // 2. 두 번째 가게 (피자 카테고리)
        ShopDocument shop2 = ShopDocument.builder()
                .id("2")
                .name("도미노피자 서초점")
                .categories(List.of(
                        new ShopDocument.CategoryInfo(3L, "PIZZA", "피자")
                ))
                .build();

        // 3. 저장 실행 (indexName "shops"에 저장됨)
        elasticsearchOperations.save(shop1);
        elasticsearchOperations.save(shop2);

        System.out.println("가짜 데이터 저장 완료!");
    }


    @Test
    @DisplayName("사장님 가게 상세는 수정 화면에 필요한 주소, 카테고리, 배달 방식을 반환한다")
    void getOwnerShopDetail() throws Exception {
        int seq = SEQ.incrementAndGet();
        Owner owner = saveOwner(seq);
        Category chicken = categoryRepository.save(Category.createCategory("SHOP_TEST_A_" + seq, "치킨", seq));
        Category pizza = categoryRepository.save(Category.createCategory("SHOP_TEST_B_" + seq, "피자", seq));
        Shop shop = shopRepository.save(Shop.createShop(
                owner,
                List.of(DeliveryType.DREAM_DELIVERY, DeliveryType.TAKEOUT),
                List.of(chicken, pizza),
                Address.of("Seoul", "pungnab", "56789"),
                "상세테스트" + seq));

        OwnerShopDetailResponseDto detail = shopService.getOwnerShopDetail(shop.getId(), owner.getId());

        assertThat(detail.getShopId()).isEqualTo(shop.getId());
        assertThat(detail.getShopName()).isEqualTo("상세테스트" + seq);
        assertThat(detail.getCity()).isEqualTo("Seoul");
        assertThat(detail.getStreet()).isEqualTo("pungnab");
        assertThat(detail.getZipcode()).isEqualTo("56789");
        assertThat(detail.getCategoryIds()).containsExactlyInAnyOrder(chicken.getId(), pizza.getId());
        assertThat(detail.getDeliveryTypes())
                .containsExactlyInAnyOrder(DeliveryType.DREAM_DELIVERY, DeliveryType.TAKEOUT);
    }

    @Test
    @DisplayName("다른 사장님의 가게 상세는 조회할 수 없다")
    void getOwnerShopDetail_otherOwner() {
        int seq = SEQ.incrementAndGet();
        Owner owner = saveOwner(seq);
        Owner other = saveOwner(SEQ.incrementAndGet());
        Category category = categoryRepository.save(Category.createCategory("SHOP_TEST_C_" + seq, "치킨", seq));
        Shop shop = shopRepository.save(Shop.createShop(
                owner, List.of(DeliveryType.TAKEOUT), List.of(category),
                Address.of("Seoul", "pungnab", "56789"), "권한테스트" + seq));

        assertThatThrownBy(() -> shopService.getOwnerShopDetail(shop.getId(), other.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("가게를 수정할 때 기존 카테고리를 유지해도 수정된다")
    void updateShop_keepCategory() throws Exception {
        int seq = SEQ.incrementAndGet();
        Owner owner = saveOwner(seq);
        Category chicken = categoryRepository.save(Category.createCategory("SHOP_TEST_D_" + seq, "치킨", seq));
        Category pizza = categoryRepository.save(Category.createCategory("SHOP_TEST_E_" + seq, "피자", seq));
        Shop shop = shopRepository.save(Shop.createShop(
                owner, List.of(DeliveryType.TAKEOUT), List.of(chicken),
                Address.of("Seoul", "pungnab", "56789"), "수정테스트" + seq));

        ShopUpdateRequestDto request = new ShopUpdateRequestDto();
        ReflectionTestUtils.setField(request, "shopName", "수정됨" + seq);
        ReflectionTestUtils.setField(request, "city", "Busan");
        ReflectionTestUtils.setField(request, "street", "haeundae");
        ReflectionTestUtils.setField(request, "zipcode", "12345");
        ReflectionTestUtils.setField(request, "categoryIds", List.of(chicken.getId(), pizza.getId()));
        ReflectionTestUtils.setField(request, "deliveryTypes", List.of(DeliveryType.TAKEOUT, DeliveryType.SHOP_DELIVERY));

        shopService.updateShop(shop.getId(), owner.getId(), request);

        OwnerShopDetailResponseDto detail = shopService.getOwnerShopDetail(shop.getId(), owner.getId());
        assertThat(detail.getShopName()).isEqualTo("수정됨" + seq);
        assertThat(detail.getCity()).isEqualTo("Busan");
        assertThat(detail.getCategoryIds()).containsExactlyInAnyOrder(chicken.getId(), pizza.getId());
        assertThat(detail.getDeliveryTypes())
                .containsExactlyInAnyOrder(DeliveryType.TAKEOUT, DeliveryType.SHOP_DELIVERY);
    }

    private Owner saveOwner(int seq) {
        return ownerRepository.save(Owner.builder()
                .email("shop-test-owner" + seq + "@test.com")
                .password("1111")
                .businessNumber("111-" + seq)
                .role(MemberRole.OWNER)
                .build());
    }
}
