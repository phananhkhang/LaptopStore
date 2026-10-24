package org.akira.laptopstore.procurement.repository;

import jakarta.persistence.LockModeType;
import org.akira.laptopstore.procurement.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
// Lấy toàn bộ danh sách phiếu nhập (mới nhất lên đầu), nạp sẵn supplier và items
    @EntityGraph(attributePaths = {"supplier", "items"})
    List<PurchaseOrder> findAllByOrderByCreatedAtDescIdDesc();

    // Lọc danh sách phiếu nhập theo trạng thái (DRAFT, PARTIALLY_RECEIVED, RECEIVED, CANCELLED)
    @EntityGraph(attributePaths = {"supplier", "items"})
    List<PurchaseOrder> findByStatusIgnoreCaseOrderByCreatedAtDescIdDesc(String status);

    // Lấy danh sách phiếu nhập của một nhà cung cấp cụ thể
    @EntityGraph(attributePaths = {"supplier", "items"})
    List<PurchaseOrder> findBySupplierIdOrderByCreatedAtDescIdDesc(Long supplierId);

    // Xem chi tiết 1 phiếu nhập kèm đầy đủ supplier và items
    @EntityGraph(attributePaths = {"supplier", "items"})
    @Query("SELECT po FROM PurchaseOrder po WHERE po.id = :id")
    Optional<PurchaseOrder> findDetailById(@Param("id") Long id);

    // Khóa bản ghi PurchaseOrder (SELECT ... FOR UPDATE) khi thực hiện Nhận hàng (Receive) hoặc Hủy phiếu
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);
}
