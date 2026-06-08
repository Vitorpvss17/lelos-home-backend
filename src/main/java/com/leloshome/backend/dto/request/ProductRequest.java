package com.leloshome.backend.dto.request;

import com.leloshome.backend.domain.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        @NotNull ProductType type,
        @DecimalMin("0.01") BigDecimal salePrice,
        @DecimalMin("0.01") BigDecimal rentPrice,
        List<String> imageUrls,
        @Min(0) Integer stockQty,
        UUID categoryId
) {}
