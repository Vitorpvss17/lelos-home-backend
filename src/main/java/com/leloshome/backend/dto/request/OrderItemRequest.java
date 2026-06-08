package com.leloshome.backend.dto.request;

import com.leloshome.backend.domain.ItemMode;
import com.leloshome.backend.domain.OrderItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequest(
        @NotNull OrderItemType itemType,
        UUID productId,
        UUID kitId,
        @NotNull @Min(1) Integer quantity,
        @NotNull ItemMode itemMode
) {}
