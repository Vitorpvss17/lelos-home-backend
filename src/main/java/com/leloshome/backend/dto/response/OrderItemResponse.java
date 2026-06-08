package com.leloshome.backend.dto.response;

import com.leloshome.backend.domain.ItemMode;
import com.leloshome.backend.domain.OrderItem;
import com.leloshome.backend.domain.OrderItemType;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        OrderItemType itemType,
        UUID productId,
        String productName,
        UUID kitId,
        String kitName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        ItemMode itemMode
) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(
                i.getId(),
                i.getItemType(),
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProduct() != null ? i.getProduct().getName() : null,
                i.getKit() != null ? i.getKit().getId() : null,
                i.getKit() != null ? i.getKit().getName() : null,
                i.getQuantity(),
                i.getUnitPrice(),
                i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())),
                i.getItemMode()
        );
    }
}
