package com.dreamhyuk.dream_order.domain.product.service;

import com.dreamhyuk.dream_order.domain.inventory.service.InventoryService;
import com.dreamhyuk.dream_order.domain.product.Product;
import com.dreamhyuk.dream_order.domain.product.ProductCategory;
import com.dreamhyuk.dream_order.domain.product.ProductSaleStatus;
import com.dreamhyuk.dream_order.domain.product.dto.ProductResponseDto;
import com.dreamhyuk.dream_order.domain.product.repository.ProductCategoryRepository;
import com.dreamhyuk.dream_order.domain.product.repository.ProductRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductServiceTest {

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired private ProductService productService;
    @Autowired private InventoryService inventoryService;
    @Autowired private ProductCategoryRepository productCategoryRepository;
    @Autowired private ProductRepository productRepository;

    @Test
    @DisplayName("카테고리별 상품을 조회하고, 가용 재고가 없으면 품절로 표시한다")
    void getProducts() {
        ProductCategory snack = productCategoryRepository.save(ProductCategory.createProductCategory("과자", 1));
        ProductCategory drink = productCategoryRepository.save(ProductCategory.createProductCategory("음료", 2));

        Product inStock = productRepository.save(Product.createProduct(snack, "새우깡", 1500));
        Product allReserved = productRepository.save(Product.createProduct(snack, "초코파이", 4800));
        Product noInventory = productRepository.save(Product.createProduct(snack, "감자칩", 1700));
        productRepository.save(Product.createProduct(drink, "콜라", 3200));

        inventoryService.create(inStock.getId(), 5);
        inventoryService.create(allReserved.getId(), 2);
        //남은 재고를 주문이 모두 선점한 상태
        inventoryService.reserve(700_001L, Map.of(allReserved.getId(), 2));

        List<ProductResponseDto> products = productService.getProducts(snack.getId());

        assertThat(products)
                .extracting("productId", "name", "price", "saleStatus", "soldOut")
                .containsExactly(
                        Tuple.tuple(inStock.getId(), "새우깡", 1500, ProductSaleStatus.ON_SALE, false),
                        Tuple.tuple(allReserved.getId(), "초코파이", 4800, ProductSaleStatus.ON_SALE, true),
                        Tuple.tuple(noInventory.getId(), "감자칩", 1700, ProductSaleStatus.ON_SALE, true));

        assertThat(productService.getCategories())
                .extracting("categoryId")
                .contains(snack.getId(), drink.getId());
    }

    @Test
    @DisplayName("상품 하나를 조회하고, 없는 상품이면 예외가 발생한다")
    void getProduct() {
        ProductCategory category = productCategoryRepository.save(ProductCategory.createProductCategory("생활용품", 3));
        Product product = productRepository.save(Product.createProduct(category, "물티슈", 2000));
        inventoryService.create(product.getId(), 1);

        ProductResponseDto found = productService.getProduct(product.getId());

        assertThat(found.getName()).isEqualTo("물티슈");
        assertThat(found.getCategoryId()).isEqualTo(category.getId());
        assertThat(found.isSoldOut()).isFalse();

        assertThatThrownBy(() -> productService.getProduct(999_999_999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
