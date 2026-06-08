package com.leloshome.backend.dto.request;

import com.leloshome.backend.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull OrderStatus status
) {}
