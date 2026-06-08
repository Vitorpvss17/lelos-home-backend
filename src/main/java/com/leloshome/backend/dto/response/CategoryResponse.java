package com.leloshome.backend.dto.response;

import com.leloshome.backend.domain.Category;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        String imageUrl,
        Boolean active,
        LocalDateTime createdAt
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                c.getImageUrl(), c.getActive(), c.getCreatedAt());
    }
}
