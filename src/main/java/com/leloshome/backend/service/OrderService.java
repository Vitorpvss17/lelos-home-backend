package com.leloshome.backend.service;

import com.leloshome.backend.domain.*;
import com.leloshome.backend.dto.request.OrderItemRequest;
import com.leloshome.backend.dto.request.OrderRequest;
import com.leloshome.backend.dto.response.OrderResponse;
import com.leloshome.backend.exception.BusinessException;
import com.leloshome.backend.exception.ResourceNotFoundException;
import com.leloshome.backend.repository.KitRepository;
import com.leloshome.backend.repository.OrderRepository;
import com.leloshome.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final KitRepository kitRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public OrderResponse create(OrderRequest req) {
        Order order = Order.builder()
                .customerName(req.customerName())
                .customerPhone(req.customerPhone())
                .eventDate(req.eventDate())
                .notes(req.notes())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemReq : req.items()) {
            OrderItem oi = buildOrderItem(itemReq, order);
            orderItems.add(oi);
        }
        order.setItems(orderItems);

        // persist first to have the ID for the WhatsApp message
        Order saved = orderRepository.save(order);

        String waUrl = whatsAppService.buildUrl(saved);
        saved.setWhatsappUrl(waUrl);
        saved.setStatus(OrderStatus.SENT_TO_WHATSAPP);

        return OrderResponse.from(orderRepository.save(saved));
    }

    public OrderResponse findById(UUID id) {
        return OrderResponse.from(getOrThrow(id));
    }

    public Page<OrderResponse> listAll(OrderStatus status, LocalDate createdFrom, LocalDate createdTo, Pageable pageable) {
        LocalDateTime from = createdFrom != null ? createdFrom.atStartOfDay() : null;
        // intervalo inclusivo no dia: limite superior é o início do dia seguinte (exclusivo)
        LocalDateTime to = createdTo != null ? createdTo.plusDays(1).atStartOfDay() : null;

        List<Specification<Order>> filters = new ArrayList<>();
        if (status != null) {
            filters.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            filters.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            filters.add((root, query, cb) -> cb.lessThan(root.get("createdAt"), to));
        }
        Specification<Order> spec = filters.stream().reduce(Specification::and).orElse(null);

        return orderRepository.findAll(spec, pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateStatus(UUID id, OrderStatus newStatus) {
        Order order = getOrThrow(id);
        order.setStatus(newStatus);
        return OrderResponse.from(orderRepository.save(order));
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private OrderItem buildOrderItem(OrderItemRequest req, Order order) {
        OrderItem.OrderItemBuilder builder = OrderItem.builder()
                .order(order)
                .itemType(req.itemType())
                .quantity(req.quantity())
                .itemMode(req.itemMode());

        if (req.itemType() == OrderItemType.PRODUCT) {
            if (req.productId() == null) throw new BusinessException("productId obrigatório para item do tipo PRODUCT");
            Product p = productRepository.findByIdAndActiveTrue(req.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + req.productId()));
            BigDecimal price = req.itemMode() == ItemMode.RENT ? p.getRentPrice() : p.getSalePrice();
            if (price == null) throw new BusinessException("Produto '" + p.getName() + "' não possui preço para " + req.itemMode());
            builder.product(p).unitPrice(price);
        } else {
            if (req.kitId() == null) throw new BusinessException("kitId obrigatório para item do tipo KIT");
            Kit k = kitRepository.findByIdAndActiveTrue(req.kitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kit não encontrado: " + req.kitId()));
            BigDecimal price = req.itemMode() == ItemMode.RENT ? k.getRentPrice() : k.getSalePrice();
            if (price == null) throw new BusinessException("Kit '" + k.getName() + "' não possui preço para " + req.itemMode());
            builder.kit(k).unitPrice(price);
        }

        return builder.build();
    }

    private Order getOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }
}
