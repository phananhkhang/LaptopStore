package org.akira.laptopstore.procurement.service;

import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;
import org.akira.laptopstore.exception.BusinessException;

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

import org.akira.laptopstore.procurement.service.impl.PurchaseOrderServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceImplTest {

    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private PurchaseOrderMapper purchaseOrderMapper;
    @Mock private CatalogVariantQueryService catalogVariantQueryService;
    @Mock private InventoryService inventoryService;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Test
    void createPurchaseOrder_SupplierInactive_ShouldThrowException() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(1L, "Nhập hàng", List.of(
                new CreatePurchaseOrderItemRequest(10L, 50, new BigDecimal("15000000"))
        ));
        Supplier inactiveSupplier = Supplier.builder().id(1L).status("INACTIVE").build();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(inactiveSupplier));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            purchaseOrderService.createPurchaseOrder(99L, request);
        });
        assertEquals("SUPPLIER_INACTIVE", exception.getCode());
    }

    @Test
    void cancelPurchaseOrder_NotInDraft_ShouldThrowException() {
        PurchaseOrder receivedOrder = PurchaseOrder.builder().id(1L).status("RECEIVED").build();
        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(receivedOrder));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            purchaseOrderService.cancelPurchaseOrder(1L, 99L);
        });
        assertEquals("INVALID_PO_STATE", exception.getCode());
    }

    @Test
    void receivePurchaseOrder_OverReceive_ShouldThrowException() {
        PurchaseOrderItem item = PurchaseOrderItem.builder().variantId(10L).quantity(50).receivedQuantity(10).build();
        PurchaseOrder po = PurchaseOrder.builder().id(1L).status("DRAFT").items(List.of(item)).build();
        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));

        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest();
        request.setIdempotencyKey(java.util.UUID.randomUUID().toString()); // Set mock key
        request.setLines(List.of(new PurchaseStockReceipt(10L, 41)));
/*
*Mã lỗi: The constructor ReceivePurchaseOrderRequest(List.of(...)) is undefined
*Nguyên nhân: Đang cố khởi tạo object bằng constructor truyền vào 1 tham số là List.
    * Tuy nhiên, vì chúng ta vừa thêm trường String idempotencyKey vào ReceivePurchaseOrderRequest, cấu trúc của @AllArgsConstructor (nếu dùng Lombok) đã thay đổi (cần 2 tham số).
    * Nếu class chỉ có @NoArgsConstructor, thì việc gọi new ReceivePurchaseOrderRequest(List) là hoàn toàn sai.
→ Giải pháp: Thay vì dùng constructor 1 tham số, hãy khởi tạo object rỗng và dùng Setter để set cả lines lẫn idempotencyKey
→ Thay đoạn code cũ: ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest(List.of(new PurchaseStockReceipt(10L, 41)));
→ Thành đoạn code mới:
    ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest();
    request.setIdempotencyKey(java.util.UUID.randomUUID().toString()); // Set mock key
    request.setLines(List.of(new PurchaseStockReceipt(10L, 41)));
→ Ở phía dưới, thay đổi tương tự với những chỗ nào khai báo ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest(List.of(new PurchaseStockReceipt(.....)));
*/

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(1L, 99L, request);
        });
        assertEquals("OVER_RECEIVE", exception.getCode());
        verify(inventoryService, never()).receivePurchase(anyLong(), anyLong(), anyString(), anyList());
/*
*Bài test này đang kiểm tra trường hợp "Nhận lố số lượng hàng" (Over Receive).
*Trong thực tế, class PurchaseOrderServiceImpl đã hoạt động ĐÚNG: nó phát hiện số lượng lố, ném ra ngoại lệ BusinessException và dừng ngay lập tức trước khi kịp chạy xuống dòng gọi inventoryService.receivePurchase(...).
*Tuy nhiên, trong file Test ở dòng 99, lệnh verify(inventoryService).receivePurchase(...) để kỳ vọng rằng hàm này phải được gọi.
*Do code đã văng lỗi và dừng sớm, hàm mock này không bao giờ được chạm tới (zero interactions), dẫn đến test bị fail.
→ Giải pháp: Thay vì verify hàm được gọi, đối với kịch bản Exception, em phải verify rằng hàm KHÔNG BAO GIỜ được gọi để đảm bảo an toàn.
    → import static org.mockito.Mockito.* → verify(inventoryService, never()).receivePurchase(...........)
*/
    }

    @Test
    void receivePurchaseOrder_AlreadyReceivedOrCancelled_ShouldThrowException() {
        PurchaseOrder po = PurchaseOrder.builder().id(1L).status("CANCELLED").build();
        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));

        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest();
        request.setIdempotencyKey(java.util.UUID.randomUUID().toString()); // Set mock key
        request.setLines(List.of(new PurchaseStockReceipt(10L, 5)));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(1L, 99L, request);
        });
        assertEquals("INVALID_PO_STATE", exception.getCode());
    }

    @Test
    void receivePurchaseOrder_PartialReceive_ShouldChangeStatusToPartiallyReceived() {
        PurchaseOrderItem item = PurchaseOrderItem.builder().variantId(10L).quantity(50).receivedQuantity(0).build();
        PurchaseOrder po = PurchaseOrder.builder().id(1L).status("DRAFT").items(List.of(item)).build();

        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest();
        request.setIdempotencyKey(java.util.UUID.randomUUID().toString()); // Set mock key
        request.setLines(List.of(new PurchaseStockReceipt(10L, 20)));

        purchaseOrderService.receivePurchaseOrder(1L, 99L, request);

        assertEquals("PARTIALLY_RECEIVED", po.getStatus());
        assertEquals(20, item.getReceivedQuantity());
        verify(inventoryService, times(1)).receivePurchase(eq(1L), eq(99L), anyString(), anyList());
/*
*Mã lỗi: not applicable for the arguments (Long, Long, String) (Thiếu tham số List)
*Nguyên nhân: Interface InventoryService đã được đổi chữ ký hàm thành 4 tham số: (Long purchaseOrderId, Long actorId, String idempotencyKey, List<PurchaseStockReceipt> lines).
    * Trong file Test, các lệnh verify(inventoryService).receivePurchase(...) hoặc doNothing().when(...) vẫn đang sử dụng cấu trúc cũ (3 tham số) khiến trình biên dịch báo lỗi truyền sai kiểu dữ liệu và thiếu tham số.
→ Giải pháp: Tìm đến các dòng và cập nhật lại tham số truyền vào hàm Mock/Verify. → Phải chèn thêm tham số String vào vị trí thứ 3.
    * Nếu đang dùng Mockito verify:
        CŨ (Dòng 120, 139): verify(inventoryService).receivePurchase(eq(1L), eq(1L), anyList());
        MỚI:
            import static org.mockito.ArgumentMatchers.anyString;
            import static org.mockito.ArgumentMatchers.anyList;
            ...
            verify(inventoryService, times(1)).receivePurchase(eq(1L), eq(1L), anyString(), anyList());
*/
    }

    @Test
    void receivePurchaseOrder_FullReceive_ShouldChangeStatusToReceived() {
        PurchaseOrderItem item = PurchaseOrderItem.builder().variantId(10L).quantity(50).receivedQuantity(40).build();
        PurchaseOrder po = PurchaseOrder.builder().id(1L).status("PARTIALLY_RECEIVED").items(List.of(item)).build();

        when(purchaseOrderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(po));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest();
        request.setIdempotencyKey(java.util.UUID.randomUUID().toString()); // Set mock key
        request.setLines(List.of(new PurchaseStockReceipt(10L, 10)));

        purchaseOrderService.receivePurchaseOrder(1L, 99L, request);

        assertEquals("RECEIVED", po.getStatus());
        assertEquals(50, item.getReceivedQuantity());
        verify(inventoryService, times(1)).receivePurchase(eq(1L), eq(99L), anyString(), anyList());

    }
}