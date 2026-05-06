package com.dreamhyuk.dream_order.domain.category.service;

import com.dreamhyuk.dream_order.domain.category.CategoryRepository;
import com.dreamhyuk.dream_order.domain.category.dto.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;


//    @Cacheable(value = "categories") //Redis나 로컬 캐시에 저장
    public List<CategoryResponseDto> getCategories() {
        return categoryRepository.findAllByOrderByPriorityAsc().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }
}
