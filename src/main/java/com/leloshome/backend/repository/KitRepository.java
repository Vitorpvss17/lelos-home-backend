package com.leloshome.backend.repository;

import com.leloshome.backend.domain.Kit;
import com.leloshome.backend.domain.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KitRepository extends JpaRepository<Kit, UUID> {
    Page<Kit> findAllByActiveTrue(Pageable pageable);
    Page<Kit> findAllByActiveTrueAndType(ProductType type, Pageable pageable);
    Optional<Kit> findByIdAndActiveTrue(UUID id);
}
