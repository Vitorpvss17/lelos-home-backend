package com.leloshome.backend.dto.response;

import com.leloshome.backend.domain.Product;
import com.leloshome.backend.domain.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        ProductType type,
        BigDecimal salePrice,
        BigDecimal rentPrice,
        List<String> imageUrls,
        Integer stockQty,
        Boolean active,
        UUID categoryId,
        String categoryName,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getType(),
                p.getSalePrice(),
                p.getRentPrice(),
                p.getImageUrls(),
                p.getStockQty(),
                p.getActive(),
                p.getCategory() != null ? p.getCategory().getId() : null,
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getCreatedAt()
        );
    }
}
