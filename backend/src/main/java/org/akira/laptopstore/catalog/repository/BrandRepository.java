package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByName(String name);

    // Chốt chặn 1 (Dùng cho Create): Trả về true nếu tên Brand đã tồn tại trong DB
    boolean existsByName(String name);

    // Chốt chặn 2 (Dùng cho Update): Trả về true nếu tên Brand bị trùng với một Brand KHÁC (khác ID)
    boolean existsByNameAndIdNot(String name, Long id);
}