package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.Product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

// Bước 1: Fetch Brand, Category, Specification và Variants
    @EntityGraph(attributePaths = {"brand", "category", "specification", "variants"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findProductAndVariantsById(Long id);

    // Bước 2: Fetch riêng Images cho Product đó
    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findProductImagesById(Long id);
}