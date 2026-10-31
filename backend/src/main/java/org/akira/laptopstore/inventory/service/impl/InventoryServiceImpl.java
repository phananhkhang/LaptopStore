package org.akira.laptopstore.inventory.service.impl;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.inventory.entity.StockMovement;
import org.akira.laptopstore.inventory.repository.StockMovementRepository;
import org.akira.laptopstore.inventory.service.InventoryService;

import org.akira.laptopstore.inventory.dto.request.OrderStockDeduction;
import org.akira.laptopstore.inventory.dto.request.PurchaseStockReceipt;
import org.akira.laptopstore.inventory.dto.response.StockMovementResponse;

import org.akira.laptopstore.catalog.dto.response.StockChangeResult;
import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;

import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;

import org.akira.laptopstore.exception.ValidationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StockMovementRepository stockMovementRepository;
    private final CatalogCheckoutService catalogCheckoutService;

    // Inject thêm Query Service của Catalog để lấy tên sản phẩm & SKU
    private final CatalogVariantQueryService catalogVariantQueryService;

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getHistoryByVariantId(Long variantId) {
        List<StockMovement> movements = stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
        return mapMovementsToResponses(movements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getHistoryByVariantId(Long variantId, Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(variantId, pageable);
        return mapMovementsToResponsesPage(movements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getAllMovements(Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapMovementsToResponsesPage(movements);
    }

    // --- CÁC HÀM MAPPER NỘI BỘ (Chống lỗi N+1 Query khi gọi chéo Module) ---

    private Page<StockMovementResponse> mapMovementsToResponsesPage(Page<StockMovement> movements) {
        // Gom tất cả variantId lại để gọi Catalog 1 lần duy nhất (Batch Fetching)
        Set<Long> variantIds = movements.stream().map(StockMovement::getVariantId).collect(Collectors.toSet());
        Map<Long, VariantStockSnapshot> variantMap = catalogVariantQueryService.getVariantStocks(variantIds).stream()
                .collect(Collectors.toMap(VariantStockSnapshot::getVariantId, v -> v));

        return movements.map(movement -> buildResponse(movement, variantMap.get(movement.getVariantId())));
    }

    private List<StockMovementResponse> mapMovementsToResponses(List<StockMovement> movements) {
        Set<Long> variantIds = movements.stream().map(StockMovement::getVariantId).collect(Collectors.toSet());
        Map<Long, VariantStockSnapshot> variantMap = catalogVariantQueryService.getVariantStocks(variantIds).stream()
                .collect(Collectors.toMap(VariantStockSnapshot::getVariantId, v -> v));

        return movements.stream()
                .map(movement -> buildResponse(movement, variantMap.get(movement.getVariantId())))
                .toList();
    }

    private StockMovementResponse buildResponse(StockMovement movement, VariantStockSnapshot variant) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .variantId(movement.getVariantId())
                .sku(variant != null ? variant.getSku() : "N/A")
                .productName(variant != null ? variant.getProductName() : "Unknown Product")
                .variantName(variant != null ? variant.getVariantName() : "")
                .type(movement.getType())
                .delta(movement.getDelta())
                .quantityBefore(movement.getQuantityBefore())
                .quantityAfter(movement.getQuantityAfter())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .operationKey(movement.getOperationKey())
                .note(movement.getNote())
                .createdByUserId(movement.getCreatedByUserId())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    // ========================================================================
    // CÁC HÀM NGHIỆP VỤ GHI (ĐÃ ĐƯỢC CHUẨN HÓA)
    // ========================================================================

    @Override
    public StockMovement recordMovement(StockMovement movement) {
        if (stockMovementRepository.findByOperationKey(movement.getOperationKey()).isPresent()) {
            throw new ValidationException("Biến động kho đã được ghi nhận trước đó!");
        }
        return stockMovementRepository.save(movement);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deductForOrder(Long orderId, Long actorId, List<OrderStockDeduction> lines) {
        validateDeductionRequest(orderId, lines);
        for (OrderStockDeduction line : sortedByVariant(lines)) {
            validateLine(line);
            String operationKey = operationKey(orderId, line.getVariantId());
            if (alreadyProcessed(operationKey)) {
                continue;
            }
            StockChangeResult stockChange = catalogCheckoutService.deductStock(
                    line.getVariantId(), line.getQuantity());
            stockMovementRepository.save(createOrderMovement(
                    orderId, actorId, line.getVariantId(), operationKey, stockChange, "SALE", -line.getQuantity()));
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void restoreForOrder(Long orderId, Long actorId, List<OrderStockDeduction> lines) {
        validateDeductionRequest(orderId, lines);
        for (OrderStockDeduction line : sortedByVariant(lines)) {
            validateLine(line);
            // Khóa chống lặp riêng cho luồng hoàn kho
            String operationKey = "restore-" + orderId + "-variant-" + line.getVariantId();
            if (alreadyProcessed(operationKey)) {
                continue;
            }
            // Hoàn tồn kho là CỘNG lại số lượng đã trừ (delta > 0)
            StockChangeResult stockChange = catalogCheckoutService.adjustStock(
                    line.getVariantId(), line.getQuantity());
            stockMovementRepository.save(createOrderMovement(
                    orderId, actorId, line.getVariantId(), operationKey, stockChange, "CANCEL_RESTORE", line.getQuantity()));
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void receivePurchase(Long purchaseOrderId, Long actorId, String idempotencyKey, List<PurchaseStockReceipt> lines) {
        if (purchaseOrderId == null || purchaseOrderId <= 0 || lines == null || lines.isEmpty()) {
            throw new ValidationException("Bắt buộc phải có thông tin phiếu nhập và danh sách sản phẩm");
        }

        // Sắp xếp ID tăng dần để chống Deadlock
        List<PurchaseStockReceipt> sortedLines = lines.stream()
                .sorted(Comparator.comparing(PurchaseStockReceipt::getVariantId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        for (PurchaseStockReceipt line : sortedLines) {
            if (line == null || line.getVariantId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new ValidationException("Danh sách biến thể nhận hàng phải hợp lệ và số lượng > 0");
            }

            // Khóa chống trùng lặp theo ID Phiếu Nhập và Variant

/*
*Tại sao đã đưa ra Giải pháp chuẩn Industry (Chuẩn Enterprise): "Client-Generated Idempotency Key" nhưng mà lại xóa bỏ hàm System.currentTimeMillis() và thay thế `purchaseOrderId` thành `idempotencyKey`?
    *1. Tại sao phải XÓA BỎ triệt để System.currentTimeMillis()?
        - Bản chất của lỗi trùng lặp (Double-click / Retry) là Client gửi cùng một data thành 2 request riêng biệt nhưng cách nhau một khoảng thời gian rất nhỏ (ví dụ vài chục mili-giây).
        - Nếu dùng System.currentTimeMillis():
            + Request đầu tiên sẽ tạo ra khóa: ...-1730000000001
            + Request thứ 2 (do double-click) sinh ra sau đó 20ms sẽ tạo ra khóa: ...-1730000000021
            → Hậu quả: Database nhìn thấy 2 khóa này khác nhau hoàn toàn.
            → Hàm alreadyProcessed() bị vô hiệu hóa vì nó tưởng đây là 2 lần nhập hàng khác nhau
                → Sinh ra nhiều dòng StockMovement (làm rớt bài test).
        * Nguyên tắc tối thượng của Idempotency là:
                Các request lặp lại (do lỗi mạng hoặc double-click) của CÙNG MỘT HÀNH ĐỘNG phải sinh ra CÙNG MỘT KHÓA.
                Thời gian là thứ luôn trôi đi, nên tuyệt đối không được dùng thời gian làm khóa Idempotency.
    * 2. Tại sao lại dùng idempotencyKey để thay thế (và có cần giữ lại purchaseOrderId không)?
        *Version 1 (Cũ nhất): receipt - purchaseOrderId - variantId
            → Yếu điểm:
                - Nếu phiếu nhập PO#1 có 10 cái Laptop.
                - Hôm nay em nhận 5 cái.
                - Ngày mai em vào nhận 5 cái còn lại.
                - Hệ thống sẽ chặn đợt nhận ngày mai, vì khóa receipt-1-variant-1 đã bị lưu trong DB từ hôm nay rồi.
                - Vĩnh viễn không thể nhận hàng đợt 2.
        *Version 2 (Bị lỗi test): receipt - purchaseOrderId - variantId - currentTimeMillis
            → Yếu điểm: Giải quyết được vụ nhận hàng nhiều đợt, nhưng lại bị lỗi Double-click như mình vừa phân tích ở trên.
        *Version 3 (Chuẩn Enterprise): receipt - idempotencyKey - variantId
            - idempotencyKey là một chuỗi ngẫu nhiên (UUID) do Frontend tạo ra một lần duy nhất khi người dùng mở popup "Nhận hàng".
            - Nếu user bấm "Lưu" liên tục 5 lần, Frontend vẫn chỉ gửi duy nhất 1 mã UUID đó xuống Backend.
                → Backend thấy 5 request có chung UUID → Chỉ xử lý cái đầu tiên, chặn 4 cái sau (Chống được Double-click).
            - Ngày mai, user mở popup lên để nhận đợt 2, Frontend sẽ sinh ra một UUID hoàn toàn mới.
                → Backend thấy UUID mới → Hiểu đây là một đợt nhập hàng mới và cho phép chạy (Giải quyết được vụ nhập hàng nhiều đợt).
*Về mặt logic, bản thân cái idempotencyKey (UUID) đã là độc nhất vô nhị trên toàn thế giới rồi.
    → Việc nối thêm variantId vào chỉ để tách riêng từng dòng sản phẩm trong mảng lines thành các record StockMovement riêng biệt.
    → Em hoàn toàn có thể giữ lại chữ purchaseOrderId cho dễ đọc trong Database (ví dụ: receipt-PO1-UUID-variant1), nhưng về mặt logic cốt lõi để khóa chống trùng lặp hoạt động, thì UUID mới là nhân tố quyết định.
                */
            // SỬA LỖI: Thêm System.currentTimeMillis() để đảm bảo mỗi lần nhận hàng (dù cùng 1 phiếu) đều có operationKey duy nhất
            String operationKey = "receipt-" + idempotencyKey + "-variant-" + line.getVariantId();
            if (alreadyProcessed(operationKey)) {
                continue;
                // Bỏ qua nếu đã xử lý rồi (Idempotency)
                // Chặn đứng double-click hoàn hảo!
            }

            // Gọi catalog để TĂNG tồn kho
            StockChangeResult stockChange = catalogCheckoutService.adjustStock(
                    line.getVariantId(), line.getQuantity());

            // Ghi nhật ký StockMovement
            StockMovement movement = StockMovement.builder()
                    .variantId(line.getVariantId())
                    .type("PURCHASE_RECEIPT")
                    .delta(line.getQuantity())
                    .quantityBefore(stockChange.getQuantityBefore())
                    .quantityAfter(stockChange.getQuantityAfter())
                    .referenceType("PURCHASE_ORDER")
                    .referenceId(purchaseOrderId.toString())
                    .operationKey(operationKey)
                    .note("Nhận hàng từ phiếu nhập #" + purchaseOrderId)
                    .createdByUserId(actorId)
                    .build();

            stockMovementRepository.save(movement);
        }
    }

    @Override
    @Transactional
    public void adjustStock(Long variantId, Integer delta, String reason, Long actorId) {
        if (delta == null || delta == 0) {
            throw new ValidationException("Delta điều chỉnh kho phải khác 0");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Bắt buộc phải nhập lý do khi điều chỉnh kho thủ công");
        }

        StockChangeResult stockChange = catalogCheckoutService.adjustStock(variantId, delta);
        String operationKey = "adj-" + variantId + "-" + System.currentTimeMillis();

        StockMovement movement = StockMovement.builder()
                .variantId(variantId)
                .type(delta > 0 ? "IN" : "OUT")
                .delta(delta)
                .quantityBefore(stockChange.getQuantityBefore())
                .quantityAfter(stockChange.getQuantityAfter())
                .referenceType("MANUAL_ADJUSTMENT")
                .referenceId("N/A")
                .operationKey(operationKey)
                .note(reason)
                .createdByUserId(actorId)
                .build();

        stockMovementRepository.save(movement);
    }

    // --- CÁC HÀM VALIDATE VÀ TẠO MOVEMENT HỖ TRỢ ---

    private void validateDeductionRequest(Long orderId, List<OrderStockDeduction> lines) {
        if (orderId == null || orderId <= 0 || lines == null || lines.isEmpty()) {
            throw new ValidationException("Bắt buộc phải có thông tin đơn hàng và danh sách sản phẩm");
        }
    }

    private List<OrderStockDeduction> sortedByVariant(List<OrderStockDeduction> lines) {
        return lines.stream()
                .sorted(Comparator.comparing(OrderStockDeduction::getVariantId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void validateLine(OrderStockDeduction line) {
        if (line == null || line.getVariantId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
            throw new ValidationException("Danh sách biến thể phải chứa ID và số lượng hợp lệ (> 0)");
        }
    }

    private boolean alreadyProcessed(String operationKey) {
        return stockMovementRepository.findByOperationKey(operationKey).isPresent();
    }

    private String operationKey(Long orderId, Long variantId) {
        return "order-" + orderId + "-variant-" + variantId;
    }

    // Đã refactor lại để tái sử dụng cho cả Sale và Cancel_Restore
    private StockMovement createOrderMovement(Long orderId, Long actorId, Long variantId,
                                            String operationKey, StockChangeResult stockChange, 
                                            String type, int delta) {
        return StockMovement.builder()
                .variantId(variantId)
                .type(type)
                .delta(delta)
                .quantityBefore(stockChange.getQuantityBefore())
                .quantityAfter(stockChange.getQuantityAfter())
                .referenceType("ORDER")
                .referenceId(orderId.toString())
                .operationKey(operationKey)
                .createdByUserId(actorId)
                .build();
    }
}