package com.leloshome.backend.repository;

import com.leloshome.backend.domain.Product;
import com.leloshome.backend.domain.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByActiveTrue(Pageable pageable);

    Page<Product> findAllByActiveTrueAndCategoryId(UUID categoryId, Pageable pageable);

    Page<Product> findAllByActiveTrueAndType(ProductType type, Pageable pageable);

    Page<Product> findAllByActiveTrueAndCategoryIdAndType(UUID categoryId, ProductType type, Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(UUID id);
}
