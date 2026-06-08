package com.leloshome.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record OrderRequest(
        @NotBlank String customerName,
        String customerPhone,
        LocalDate eventDate,
        String notes,
        @NotEmpty @Valid List<OrderItemRequest> items
) {}
