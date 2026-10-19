package com.dreamhyuk.dream_order.domain.product.repository;

import com.dreamhyuk.dream_order.domain.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findAllByOrderByPriorityAsc();
}
