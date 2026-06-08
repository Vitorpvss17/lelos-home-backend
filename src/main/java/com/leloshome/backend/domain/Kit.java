package com.leloshome.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Kit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ProductType type;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "rent_price", precision = 10, scale = 2)
    private BigDecimal rentPrice;

    @ElementCollection
    @CollectionTable(name = "kit_images", joinColumns = @JoinColumn(name = "kit_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KitItem> items = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
