package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
    Optional<ProductSpecification> findByProductId(Long productId);
}