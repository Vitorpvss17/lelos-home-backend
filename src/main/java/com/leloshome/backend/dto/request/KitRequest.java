package com.leloshome.backend.dto.request;

import com.leloshome.backend.domain.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record KitRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        @NotNull ProductType type,
        @DecimalMin("0.01") BigDecimal salePrice,
        @DecimalMin("0.01") BigDecimal rentPrice,
        List<String> imageUrls,
        @Valid List<KitItemRequest> items
) {}
