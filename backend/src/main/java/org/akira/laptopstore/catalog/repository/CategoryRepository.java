package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    // Radar 1 (Dùng cho Create): Quét xem tên danh mục đã có ai xài chưa
    boolean existsByName(String name);

    // Radar 2 (Dùng cho Update): Quét xem tên danh mục có bị trùng với một danh mục KHÁC (khác ID) không
    boolean existsByNameAndIdNot(String name, Long id);
}