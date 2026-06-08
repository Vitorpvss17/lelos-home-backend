package com.leloshome.backend.service;

import com.leloshome.backend.domain.Order;
import com.leloshome.backend.domain.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class WhatsAppService {

    @Value("${whatsapp.number}")
    private String whatsappNumber;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String buildUrl(Order order) {
        String message = buildMessage(order);
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        return "https://wa.me/" + whatsappNumber + "?text=" + encoded;
    }

    private String buildMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá! Gostaria de finalizar meu pedido:\n\n");
        sb.append("*Pedido #").append(order.getId().toString(), 0, 8).append("*\n");

        if (order.getEventDate() != null) {
            sb.append("📅 Data do evento: ").append(order.getEventDate().format(DATE_FMT)).append("\n");
        }
        if (order.getCustomerName() != null) {
            sb.append("👤 Nome: ").append(order.getCustomerName()).append("\n");
        }

        sb.append("\n*Itens:*\n");
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            String itemName = item.getItemType().name().equals("PRODUCT")
                    ? (item.getProduct() != null ? item.getProduct().getName() : "Produto")
                    : (item.getKit() != null ? item.getKit().getName() : "Kit");

            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            String modeLabel = item.getItemMode().name().equals("RENT") ? "Aluguel" : "Venda";
            sb.append("- ").append(item.getQuantity()).append("x ")
              .append(itemName)
              .append(" (").append(modeLabel).append(")")
              .append(" — R$ ").append(String.format("%.2f", subtotal)).append("\n");
        }

        sb.append("\n*Total: R$ ").append(String.format("%.2f", total)).append("*");

        if (order.getNotes() != null && !order.getNotes().isBlank()) {
            sb.append("\n\nObservações: ").append(order.getNotes());
        }

        return sb.toString();
    }
}
