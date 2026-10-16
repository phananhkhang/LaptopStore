package org.akira.laptopstore.inventory.repository;

import org.akira.laptopstore.inventory.entity.StockMovement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    // Lấy lịch sử tồn kho của một sản phẩm cụ thể (khôn phân trang)
    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId);

    // Lấy lịch sử tồn kho của một sản phẩm cụ thể (có phân trang)
    Page<StockMovement> findByVariantIdOrderByCreatedAtDesc(Long variantId, Pageable pageable);

    // Lấy toàn bộ lịch sử biến động kho toàn hệ thống (có phân trang)
    Page<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Lọc theo loại biến động (ví dụ: SALE, IN, OUT, PURCHASE_RECEIPT) có phân trang
    Page<StockMovement> findByTypeOrderByCreatedAtDesc(String type, Pageable pageable);

    // Tìm kiếm operationKey để chống lặp (Idempotency)
    Optional<StockMovement> findByOperationKey(String operationKey);
}