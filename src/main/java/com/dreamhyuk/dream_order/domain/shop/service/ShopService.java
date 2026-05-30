package com.dreamhyuk.dream_order.domain.shop.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.dreamhyuk.dream_order.domain.category.Category;
import com.dreamhyuk.dream_order.domain.category.CategoryRepository;
import com.dreamhyuk.dream_order.domain.common.Address;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import com.dreamhyuk.dream_order.domain.review.repository.ReviewRepository;
import com.dreamhyuk.dream_order.domain.shop.Shop;
import com.dreamhyuk.dream_order.domain.shop.ShopDocument;
import com.dreamhyuk.dream_order.domain.shop.ShopRepository;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopSearchResponseDto;
import com.dreamhyuk.dream_order.domain.shop.dto.ShopUpdateRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.util.StringUtils;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ShopService {

    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ShopResponseDto getShopDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Not Found this shop"));

        return ShopResponseDto.from(shop);
    }

    @Transactional
    public Long saveShop(ShopCommand.Create command) {
        log.info("Category IDs from Command: {}", command.getCategoryIds());
        if (command.getCategoryIds() == null) {
            throw new RuntimeException("Category IDs 리스트 자체가 null입니다!");
        }

        Owner owner = ownerRepository.findById(command.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner Not Found"));

        List<Category> categories = categoryRepository.findAllById(command.getCategoryIds());
        log.info("Found categories size: {}", categories.size());

        Shop shop = Shop.createShop(
                owner,
                command.getDeliveryTypes(),
                categories,
                command.getAddress(),
                command.getShopName()
        );

        log.info("Saving shop: {}", shop.getShopName());
        shopRepository.save(shop);

        return shop.getId();
    }

    @Transactional
    public void updateShop(Long shopId, Long ownerId, ShopUpdateRequestDto request) throws AccessDeniedException {
        Shop shop = validateShopOwner(shopId, ownerId);

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        Address address = Address.of(request.getCity(), request.getStreet(), request.getZipcode());

        shop.update(
                request.getShopName(),
                address,
                categories,
                request.getDeliveryTypes());
    }

    @Transactional
    public void deleteShop(Long shopId, Long ownerId) throws AccessDeniedException {
        Shop shop = validateShopOwner(shopId, ownerId);

        shopRepository.delete(shop);
    }

    //가게 소유권 검증
    private Shop validateShopOwner(Long shopId, Long ownerId) throws AccessDeniedException {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("가게를 찾을 수 없습니다. ID: " + shopId));

        // Shop 엔티티에 저장된 ownerId와 현재 로그인한 ownerId를 비교
        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("해당 가게에 대한 관리 권한이 없습니다.");
        }

        return shop;
    }


    /**
     * ( 필터(ex. 별점 높은 순)같은 건 구현 x)
     * 1. 키워드로 검색
     * 2. 카테고리로 검색
     * ---------------------------------------
     * !! 키워드 검색과 카테고리 검색은 같이 적용되지 않는다.
     * !! 카테고리 검색 후에 키워드 검색이 이뤄진다면 키워드 검색만 적용된다.
     * !! 이 때 카테고리 검색이 빠지는 건 Service 로직을 꼬는 대신
     * !! Controller나 DTO 수준에서 데이터를 정리해 넘겨주는 게 깔끔하다.
     */
    public List<ShopSearchResponseDto> search(ShopSearchCommand command) {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();

        //1. 키워드가 검색
        if (StringUtils.hasText(command.getKeyword())) {
            boolQuery.must(m -> m.match(mt -> mt
                    .field("name")
                    .query(command.getKeyword())
                    .operator(Operator.And)));
        }

        //2. 카테고리 필터 추가
        if (command.getCategoryId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("categories.id").value(command.getCategoryId())));
        }

        //쿼리 실행 (기본 10건으로 실행)
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()))
                .build();

/*
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQuery.build()));
        // 정렬 조건 처리
        if ("RATING_DESC".equals(command.getSortType())) {
            // 별점 높은 순 정렬
            queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "averageRating"));
        } else {
            // 기본값은 검색 정확도 순(Score)으로 설정하는 것이 좋다
            queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "_score"));
        }

        // 페이징 처리 (이건 성능을 위해 넣어주는 게 좋다!)
        queryBuilder.withPageable(PageRequest.of(command.getPage(), 10));
*/

        //실행 및 결과 매핑
        SearchHits<ShopDocument> searchHits = elasticsearchOperations.search(query, ShopDocument.class);
//        SearchHits<ShopDocument> searchHits = elasticsearchOperations.search(queryBuilder.build(), ShopDocument.class);

        return searchHits.getSearchHits().stream()
                .map(hit -> ShopSearchResponseDto.from(hit.getContent()))
                .toList();
    }

}
