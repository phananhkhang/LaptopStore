package org.akira.laptopstore.procurement.service.impl;

import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;

import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import org.akira.laptopstore.inventory.dto.request.PurchaseStockReceipt;
import org.akira.laptopstore.inventory.service.InventoryService;

import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderItemRequest;
import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.request.ReceivePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.response.PurchaseOrderResponse;

import org.akira.laptopstore.procurement.entity.PurchaseOrder;
import org.akira.laptopstore.procurement.entity.PurchaseOrderItem;
import org.akira.laptopstore.procurement.entity.Supplier;

import org.akira.laptopstore.procurement.mapper.PurchaseOrderMapper;

import org.akira.laptopstore.procurement.repository.PurchaseOrderItemRepository;
import org.akira.laptopstore.procurement.repository.PurchaseOrderRepository;
import org.akira.laptopstore.procurement.repository.SupplierRepository;

import org.akira.laptopstore.procurement.service.PurchaseOrderService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    // Gọi chéo qua Catalog để kiểm tra mã hàng (variantId) có tồn tại không
    private final CatalogVariantQueryService catalogVariantQueryService;

    // Gọi chéo qua Inventory để cộng tồn kho khi nhận hàng (Phần này sẽ làm ở Mục 3)
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(Long actorId, CreatePurchaseOrderRequest request) {
        // 1. Kiểm tra nhà cung cấp có tồn tại và đang hoạt động không
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + request.getSupplierId()));

        if (!"ACTIVE".equalsIgnoreCase(supplier.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SUPPLIER_INACTIVE", 
                    "Nhà cung cấp đang bị khóa hoặc ngưng hoạt động. Không thể lập phiếu nhập!");
        }

        // 2. Gom nhóm các mặt hàng trùng lặp (nếu nhân viên lỡ tay gõ 2 dòng cùng 1 sản phẩm)
        Map<Long, CreatePurchaseOrderItemRequest> itemMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        CreatePurchaseOrderItemRequest::getVariantId,
                        item -> item,
                        (existing, replacement) -> {
                            existing.setQuantity(existing.getQuantity() + replacement.getQuantity());
                            return existing;
                        }
                ));

        // 3. Gọi chéo sang Catalog để kiểm tra biến thể có thực sự tồn tại không
        List<Long> variantIds = new ArrayList<>(itemMap.keySet());
        List<VariantStockSnapshot> variants = catalogVariantQueryService.getVariantStocks(variantIds);
        if (variants.size() != variantIds.size()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_VARIANT", 
                    "Một hoặc nhiều biến thể sản phẩm không tồn tại trong hệ thống Catalog!");
        }

        // 4. Tạo phiếu nhập nháp (DRAFT)
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(supplier)
                .createdByUserId(actorId)
                .status("DRAFT")
                .note(request.getNote())
                .build();

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        // 5. Lưu chi tiết các dòng của phiếu nhập
        List<PurchaseOrderItem> orderItems = itemMap.values().stream().map(req -> PurchaseOrderItem.builder()
                .purchaseOrder(savedOrder)
                .variantId(req.getVariantId())
                .quantity(req.getQuantity())
                .receivedQuantity(0) // Mới tạo nháp, chưa có hàng nào được nhận
                .unitCost(req.getUnitCost())
                .build()).toList();

        purchaseOrderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);

        return purchaseOrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id, Long actorId) {
        // 1. Khóa phiếu nhập bằng PESSIMISTIC_WRITE để tránh nhân viên khác đang bấm nhận hàng
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        // 2. Chốt chặn: Chỉ được hủy khi phiếu đang ở trạng thái DRAFT
        if (!"DRAFT".equalsIgnoreCase(purchaseOrder.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PO_STATE", 
                    "Không thể hủy phiếu nhập đã nhận hàng hoặc đã bị hủy trước đó!");
        }

        // 3. Cập nhật trạng thái
        purchaseOrder.setStatus("CANCELLED");
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        return purchaseOrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
    }

/*
======================== ĐÃ XÓA CODE CŨ VÀ CẬP NHẬP CODE MỚI - Hàm receivePurchaseOrder =======================
*Hàm receivePurchaseOrder CŨ có một lỗ hổng logic. Nếu Frontend (hoặc ai đó dùng Postman) gửi một request nhận hàng nhưng danh sách request.getLines() bị rỗng [], hoặc gửi danh sách toàn số lượng 0 (ví dụ: [{variantId: 1, quantity: 0}]):
    1. Code vẫn chạy qua bước gom nhóm receiveMap.
    2. Phiếu nhập nháp (DRAFT) sẽ bị đổi trạng thái thành PARTIALLY_RECEIVED (do không nhận đủ hàng) mặc dù thực tế chẳng nhận được món nào.
    3. Sau đó, nó ném mảng rỗng sang inventoryService.receivePurchase, và bị tầng Inventory quăng lỗi ValidationException. → Lỗi văng ra làm Rollback, nhưng API trả về 500 Internal Server Error hoặc 400 Bad Request chưa rõ ràng cho Frontend.
*/

    @Override
    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(Long id, Long actorId, ReceivePurchaseOrderRequest request) {
        // 1. Khóa phiếu nhập bằng PESSIMISTIC_WRITE
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        // 2. Kiểm tra trạng thái hợp lệ
        if ("RECEIVED".equalsIgnoreCase(purchaseOrder.getStatus()) || "CANCELLED".equalsIgnoreCase(purchaseOrder.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PO_STATE",
                    "Phiếu nhập đã hoàn tất hoặc đã hủy, không thể nhận thêm hàng.");
        }

        // 3. CHẶN LỖI PAYLOAD RỖNG VÀ LỌC SỐ LƯỢNG ẢO
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD",
                    "Danh sách nhận hàng không được để trống!");
        }

        Map<Long, Integer> receiveMap = request.getLines().stream()
                .filter(line -> line.getQuantity() != null && line.getQuantity() > 0) // Loại bỏ hàng gửi lên có quantity <= 0
                .collect(Collectors.toMap(
                        PurchaseStockReceipt::getVariantId,
                        PurchaseStockReceipt::getQuantity,
                        Integer::sum
                ));

        if (receiveMap.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD", 
                    "Không có sản phẩm nào có số lượng nhận hợp lệ (> 0)!");
        }


        // 4. Kiểm tra mã hàng gửi lên có thuộc phiếu nhập này không
        Set<Long> poVariantIds = purchaseOrder.getItems().stream().map(PurchaseOrderItem::getVariantId).collect(Collectors.toSet());
        for (Long variantId : receiveMap.keySet()) {
            if (!poVariantIds.contains(variantId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_VARIANT", 
                        "Mã hàng " + variantId + " không thuộc phiếu nhập này.");
            }
        }

        // 5. Đối chiếu số lượng và cập nhật receivedQuantity
        boolean allFullyReceived = true;
        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            Integer quantityToReceive = receiveMap.get(item.getVariantId());
            if (quantityToReceive != null) {
                if (quantityToReceive <= 0) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_QUANTITY", "Số lượng nhận phải lớn hơn 0");
                }
                int remaining = item.getQuantity() - item.getReceivedQuantity();
                if (quantityToReceive > remaining) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "OVER_RECEIVE", 
                            "Số lượng nhận (" + quantityToReceive + ") vượt quá số lượng đặt hàng còn lại (" + remaining + ") cho mã hàng: " + item.getVariantId());
                }
                item.setReceivedQuantity(item.getReceivedQuantity() + quantityToReceive);
            }

            // Nếu có bất kỳ món nào chưa nhận đủ số lượng đặt, thì phiếu này chưa RECEIVED toàn phần
            if (item.getReceivedQuantity() < item.getQuantity()) {
                allFullyReceived = false;
            }
        }

        // 6. Cập nhật trạng thái phiếu nhập
        if (allFullyReceived) {
            purchaseOrder.setStatus("RECEIVED");
            purchaseOrder.setReceivedAt(java.time.LocalDateTime.now());
        } else {
            purchaseOrder.setStatus("PARTIALLY_RECEIVED");
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        // 7. Gọi sang InventoryService để cộng tồn kho và ghi sổ cái (nằm trong cùng transaction)
        List<PurchaseStockReceipt> stockReceipts = receiveMap.entrySet().stream()
                .map(e -> new PurchaseStockReceipt(e.getKey(), e.getValue()))
                .toList();

        inventoryService.receivePurchase(purchaseOrder.getId(), actorId, request.getIdempotencyKey(), stockReceipts);

        return purchaseOrderMapper.toResponse(savedOrder);
    }
}
