package org.akira.laptopstore.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    private String cpu;
    private String gpu;
    private String screenSize;
    private String screenResolution;
    private String refreshRate;
    private String battery;
    private String weight;
    private String os;
}