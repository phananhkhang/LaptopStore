package org.akira.laptopstore.procurement.repository;

import org.akira.laptopstore.procurement.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    // Tìm các dòng chi tiết của một phiếu nhập, sắp xếp theo ID để đảm bảo thứ tự luôn ổn định
    List<PurchaseOrderItem> findByPurchaseOrderIdOrderByIdAsc(Long purchaseOrderId);
}