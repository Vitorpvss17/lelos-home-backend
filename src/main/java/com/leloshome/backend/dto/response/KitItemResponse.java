package com.leloshome.backend.dto.response;

import com.leloshome.backend.domain.KitItem;

import java.util.UUID;

public record KitItemResponse(
        UUID id,
        UUID productId,
        String productName,
        Integer quantity
) {
    public static KitItemResponse from(KitItem ki) {
        return new KitItemResponse(ki.getId(), ki.getProduct().getId(),
                ki.getProduct().getName(), ki.getQuantity());
    }
}
