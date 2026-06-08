package com.leloshome.backend.service;

import com.leloshome.backend.domain.Category;
import com.leloshome.backend.domain.Product;
import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.request.ProductRequest;
import com.leloshome.backend.dto.response.ProductResponse;
import com.leloshome.backend.exception.ResourceNotFoundException;
import com.leloshome.backend.repository.CategoryRepository;
import com.leloshome.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> listActive(UUID categoryId, ProductType type, Pageable pageable) {
        if (categoryId != null && type != null) {
            return productRepository.findAllByActiveTrueAndCategoryIdAndType(categoryId, type, pageable)
                    .map(ProductResponse::from);
        } else if (categoryId != null) {
            return productRepository.findAllByActiveTrueAndCategoryId(categoryId, pageable)
                    .map(ProductResponse::from);
        } else if (type != null) {
            return productRepository.findAllByActiveTrueAndType(type, pageable)
                    .map(ProductResponse::from);
        }
        return productRepository.findAllByActiveTrue(pageable).map(ProductResponse::from);
    }

    public Page<ProductResponse> listAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse findById(UUID id) {
        return ProductResponse.from(
                productRepository.findByIdAndActiveTrue(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id))
        );
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product product = Product.builder()
                .name(req.name())
                .description(req.description())
                .type(req.type())
                .salePrice(req.salePrice())
                .rentPrice(req.rentPrice())
                .imageUrls(req.imageUrls() != null ? new ArrayList<>(req.imageUrls()) : new ArrayList<>())
                .stockQty(req.stockQty() != null ? req.stockQty() : 0)
                .category(resolveCategory(req.categoryId()))
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest req) {
        Product product = getOrThrow(id);
        product.setName(req.name());
        product.setDescription(req.description());
        product.setType(req.type());
        product.setSalePrice(req.salePrice());
        product.setRentPrice(req.rentPrice());
        if (req.imageUrls() != null) product.setImageUrls(new ArrayList<>(req.imageUrls()));
        if (req.stockQty() != null) product.setStockQty(req.stockQty());
        product.setCategory(resolveCategory(req.categoryId()));
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse toggle(UUID id) {
        Product product = getOrThrow(id);
        product.setActive(!product.getActive());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        productRepository.deleteById(id);
    }

    private Product getOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + categoryId));
    }
}
