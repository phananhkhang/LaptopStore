package org.akira.laptopstore.procurement.service;


import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.request.ReceivePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.response.PurchaseOrderResponse;


import java.util.List;

public interface PurchaseOrderService {
    // Tạo phiếu nhập hàng mới. Mặc định luôn ở trạng thái DRAFT.
    PurchaseOrderResponse createPurchaseOrder(Long actorId, CreatePurchaseOrderRequest request);

    // Hủy phiếu nhập hàng. Chỉ được phép hủy khi phiếu đang ở trạng thái DRAFT.
    PurchaseOrderResponse cancelPurchaseOrder(Long id, Long actorId);

    // Lấy chi tiết một phiếu nhập hàng.
    PurchaseOrderResponse getPurchaseOrderById(Long id);

    // Lấy danh sách toàn bộ phiếu nhập hàng, sắp xếp mới nhất lên đầu.
    List<PurchaseOrderResponse> getAllPurchaseOrders();

    // Nhận hàng từ phiếu nhập. Hỗ trợ nhận một phần hoặc nhận đủ.
    // Cập nhật trạng thái thành PARTIALLY_RECEIVED hoặc RECEIVED và gọi sang Inventory cộng tồn kho.
    PurchaseOrderResponse receivePurchaseOrder(Long id, Long actorId, ReceivePurchaseOrderRequest request);
}