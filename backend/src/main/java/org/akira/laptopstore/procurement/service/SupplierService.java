package org.akira.laptopstore.procurement.service;

import org.akira.laptopstore.procurement.dto.request.CreateSupplierRequest;
import org.akira.laptopstore.procurement.dto.request.UpdateSupplierRequest;
import org.akira.laptopstore.procurement.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {
    // Lấy toàn bộ danh sách nhà cung cấp
    List<SupplierResponse> getAllSuppliers();

    // Xem chi tiết 1 nhà cung cấp
    SupplierResponse getSupplierById(Long id);

    // Thêm mới nhà cung cấp
    SupplierResponse createSupplier(CreateSupplierRequest request);

    // Cập nhật thông tin nhà cung cấp
    SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request);

    // Thay đổi trạng thái (ACTIVE/INACTIVE) thay vì xóa cứng (Hard Delete)
    void toggleSupplierStatus(Long id, String status);
}
