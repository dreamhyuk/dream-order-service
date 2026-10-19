package com.dreamhyuk.dream_order.domain.product.repository;

import com.dreamhyuk.dream_order.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByCategoryIdOrderByIdAsc(Long categoryId);

    List<Product> findAllByOrderByIdAsc();
}
