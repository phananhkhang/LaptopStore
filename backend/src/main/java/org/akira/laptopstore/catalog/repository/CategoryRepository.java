package org.akira.laptopstore.catalog.repository;

import org.akira.laptopstore.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Anh viết sẵn cho em một hàm tìm kiếm bằng tên, sau này kiểu gì cũng dùng tới!
    Optional<Category> findByName(String name);
}