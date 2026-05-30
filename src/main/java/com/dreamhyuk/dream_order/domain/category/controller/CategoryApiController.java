package com.dreamhyuk.dream_order.domain.category.controller;

import com.dreamhyuk.dream_order.domain.category.dto.CategoryResponseDto;
import com.dreamhyuk.dream_order.domain.category.service.CategoryService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories() {

        return ResponseEntity.ok(categoryService.getCategories());
    }
}
