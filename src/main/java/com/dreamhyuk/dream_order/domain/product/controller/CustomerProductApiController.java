package com.dreamhyuk.dream_order.domain.product.controller;

import com.dreamhyuk.dream_order.domain.product.dto.ProductCategoryResponseDto;
import com.dreamhyuk.dream_order.domain.product.dto.ProductResponseDto;
import com.dreamhyuk.dream_order.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/mart")
public class CustomerProductApiController {

    private final ProductService productService;

    @GetMapping("/categories")
    public ResponseEntity<List<ProductCategoryResponseDto>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    //categoryId 를 생략하면 전체 상품을 조회한다
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> getProducts(
            @RequestParam(required = false) Long categoryId) {

        return ResponseEntity.ok(productService.getProducts(categoryId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }
}
