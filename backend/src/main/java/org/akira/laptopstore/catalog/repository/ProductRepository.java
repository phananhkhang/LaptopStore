package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tạm thời để trống, JPA đã lo hết các hàm save(), findById(), findAll(), deleteById().
}