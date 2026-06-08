package com.leloshome.backend.dto.response;

import com.leloshome.backend.domain.Order;
import com.leloshome.backend.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerName,
        String customerPhone,
        LocalDate eventDate,
        String notes,
        OrderStatus status,
        String whatsappUrl,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order o) {
        BigDecimal total = o.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                o.getId(),
                o.getCustomerName(),
                o.getCustomerPhone(),
                o.getEventDate(),
                o.getNotes(),
                o.getStatus(),
                o.getWhatsappUrl(),
                total,
                o.getItems().stream().map(OrderItemResponse::from).toList(),
                o.getCreatedAt()
        );
    }
}
