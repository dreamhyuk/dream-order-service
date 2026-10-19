package com.dreamhyuk.dream_order.domain.product.service;

import com.dreamhyuk.dream_order.domain.inventory.service.InventoryService;
import com.dreamhyuk.dream_order.domain.product.Product;
import com.dreamhyuk.dream_order.domain.product.dto.ProductCategoryResponseDto;
import com.dreamhyuk.dream_order.domain.product.dto.ProductResponseDto;
import com.dreamhyuk.dream_order.domain.product.repository.ProductCategoryRepository;
import com.dreamhyuk.dream_order.domain.product.repository.ProductRepository;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final InventoryService inventoryService;

    public List<ProductCategoryResponseDto> getCategories() {
        return productCategoryRepository.findAllByOrderByPriorityAsc().stream()
                .map(ProductCategoryResponseDto::from)
                .toList();
    }

    /**
     * 상품 목록 조회. categoryId 가 없으면 전체 상품을 반환한다.
     */
    public List<ProductResponseDto> getProducts(Long categoryId) {
        List<Product> products = (categoryId == null)
                ? productRepository.findAllByOrderByIdAsc()
                : productRepository.findAllByCategoryIdOrderByIdAsc(categoryId);

        //품절 표시를 위해 가용 재고를 한 번에 조회한다 (재고 행이 없는 상품은 품절로 본다)
        Map<Long, Integer> quantities = inventoryService.getQuantities(
                products.stream().map(Product::getId).toList());

        return products.stream()
                .map(product -> ProductResponseDto.of(product, quantities.getOrDefault(product.getId(), 0)))
                .toList();
    }

    public ProductResponseDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        int quantity = inventoryService.getQuantities(List.of(productId)).getOrDefault(productId, 0);

        return ProductResponseDto.of(product, quantity);
    }
}
