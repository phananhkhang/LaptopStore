package org.akira.laptopstore.procurement.repository;

import org.akira.laptopstore.procurement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Kiểm tra tên nhà cung cấp đã tồn tại chưa (không phân biệt hoa thường) khi tạo mới
    boolean existsByNameIgnoreCase(String name);

    // Kiểm tra tên nhà cung cấp có bị trùng với nhà cung cấp khác khi cập nhật không
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Lấy danh sách nhà cung cấp theo trạng thái (VD: "ACTIVE") sắp xếp mới nhất lên đầu
    List<Supplier> findByStatusIgnoreCaseOrderByIdDesc(String status);

    // Lấy toàn bộ danh sách nhà cung cấp sắp xếp theo ID giảm dần
    List<Supplier> findAllByOrderByIdDesc();
}