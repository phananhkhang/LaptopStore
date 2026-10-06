package org.akira.laptopstore.inventory.repository;

import org.akira.laptopstore.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    // Lấy lịch sử tồn kho của một sản phẩm cụ thể
    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);

    // Tìm kiếm operationKey để chống lặp
    Optional<StockMovement> findByOperationKey(String operationKey);
}