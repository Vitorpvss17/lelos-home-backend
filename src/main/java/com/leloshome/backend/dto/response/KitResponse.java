package com.leloshome.backend.dto.response;

import com.leloshome.backend.domain.Kit;
import com.leloshome.backend.domain.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record KitResponse(
        UUID id,
        String name,
        String description,
        ProductType type,
        BigDecimal salePrice,
        BigDecimal rentPrice,
        List<String> imageUrls,
        List<KitItemResponse> items,
        Boolean active,
        LocalDateTime createdAt
) {
    public static KitResponse from(Kit k) {
        return new KitResponse(
                k.getId(),
                k.getName(),
                k.getDescription(),
                k.getType(),
                k.getSalePrice(),
                k.getRentPrice(),
                k.getImageUrls(),
                k.getItems().stream().map(KitItemResponse::from).toList(),
                k.getActive(),
                k.getCreatedAt()
        );
    }
}
