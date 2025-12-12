package com.ecommerce.order_processing_system.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String productId;
    private String productName;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    @Column(columnDefinition = "jsonb")
    private String metadata;
}