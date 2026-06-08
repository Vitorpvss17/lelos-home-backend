package com.leloshome.backend.service;

import com.leloshome.backend.domain.Kit;
import com.leloshome.backend.domain.KitItem;
import com.leloshome.backend.domain.Product;
import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.request.KitItemRequest;
import com.leloshome.backend.dto.request.KitRequest;
import com.leloshome.backend.dto.response.KitResponse;
import com.leloshome.backend.exception.ResourceNotFoundException;
import com.leloshome.backend.repository.KitRepository;
import com.leloshome.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KitService {

    private final KitRepository repository;
    private final ProductRepository productRepository;

    public Page<KitResponse> listActive(ProductType type, Pageable pageable) {
        if (type != null) {
            return repository.findAllByActiveTrueAndType(type, pageable).map(KitResponse::from);
        }
        return repository.findAllByActiveTrue(pageable).map(KitResponse::from);
    }

    public Page<KitResponse> listAll(Pageable pageable) {
        return repository.findAll(pageable).map(KitResponse::from);
    }

    public KitResponse findById(UUID id) {
        return KitResponse.from(
                repository.findByIdAndActiveTrue(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Kit não encontrado: " + id))
        );
    }

    @Transactional
    public KitResponse create(KitRequest req) {
        Kit kit = Kit.builder()
                .name(req.name())
                .description(req.description())
                .type(req.type())
                .salePrice(req.salePrice())
                .rentPrice(req.rentPrice())
                .imageUrls(req.imageUrls() != null ? new ArrayList<>(req.imageUrls()) : new ArrayList<>())
                .build();
        applyItems(kit, req.items());
        return KitResponse.from(repository.save(kit));
    }

    @Transactional
    public KitResponse update(UUID id, KitRequest req) {
        Kit kit = getOrThrow(id);
        kit.setName(req.name());
        kit.setDescription(req.description());
        kit.setType(req.type());
        kit.setSalePrice(req.salePrice());
        kit.setRentPrice(req.rentPrice());
        if (req.imageUrls() != null) kit.setImageUrls(new ArrayList<>(req.imageUrls()));
        // orphanRemoval=true: limpa a coleção gerenciada e recria os itens
        kit.getItems().clear();
        applyItems(kit, req.items());
        return KitResponse.from(repository.save(kit));
    }

    @Transactional
    public KitResponse toggle(UUID id) {
        Kit kit = getOrThrow(id);
        kit.setActive(!kit.getActive());
        return KitResponse.from(repository.save(kit));
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void applyItems(Kit kit, List<KitItemRequest> items) {
        if (items == null) return;
        for (KitItemRequest itemReq : items) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + itemReq.productId()));
            KitItem kitItem = KitItem.builder()
                    .kit(kit)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .build();
            kit.getItems().add(kitItem);
        }
    }

    private Kit getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kit não encontrado: " + id));
    }
}
