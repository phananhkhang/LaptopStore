package org.akira.laptopstore.integration;

import org.akira.laptopstore.catalog.entity.Brand;
import org.akira.laptopstore.catalog.entity.Category;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.entity.ProductVariant;

import org.akira.laptopstore.catalog.repository.BrandRepository;
import org.akira.laptopstore.catalog.repository.CategoryRepository;
import org.akira.laptopstore.catalog.repository.ProductRepository;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;

import org.akira.laptopstore.inventory.entity.StockMovement;
import org.akira.laptopstore.inventory.dto.request.PurchaseStockReceipt;
import org.akira.laptopstore.inventory.repository.StockMovementRepository;

import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderItemRequest;
import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.request.ReceivePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.response.PurchaseOrderResponse;

import org.akira.laptopstore.procurement.entity.Supplier;
import org.akira.laptopstore.procurement.repository.SupplierRepository;
import org.akira.laptopstore.procurement.service.PurchaseOrderService;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test") // Chạy với profile test (thường dùng H2 database)
@Transactional // Đảm bảo test xong tự động rollback data, không làm rác DB
public class ProcurementIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductVariantRepository productVariantRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private StockMovementRepository stockMovementRepository;

    private Supplier testSupplier;
    private ProductVariant testVariant;

    @BeforeEach
    void setUp() {
        // 1. Chuẩn bị dữ liệu mồi
        testSupplier = supplierRepository.save(Supplier.builder()
                .name("Nhà cung cấp Test").status("ACTIVE").build());

        Category cat = categoryRepository.save(Category.builder().name("Test Cat").active(true).build());
        Brand brand = brandRepository.save(Brand.builder().name("Test Brand").active(true).build());
        Product product = productRepository.save(Product.builder()
                .name("Laptop Test").category(cat).brand(brand).status("ACTIVE").build());

        testVariant = productVariantRepository.save(ProductVariant.builder()
                .product(product).sku("TEST-SKU").quantity(0) // Tồn kho ban đầu = 0
                .price(BigDecimal.valueOf(1000)).status("ACTIVE").build());
    }

    @Test
    void receivePurchaseOrder_ShouldUpdateInventoryAndCatalog() {
        Long adminId = 1L;

        // BƯỚC 1: TẠO PHIẾU NHẬP NHÁP (Nhập 10 cái Laptop)
        CreatePurchaseOrderItemRequest itemReq = new CreatePurchaseOrderItemRequest();
        itemReq.setVariantId(testVariant.getId());
        itemReq.setQuantity(10);
        itemReq.setUnitCost(BigDecimal.valueOf(800));

        CreatePurchaseOrderRequest createReq = new CreatePurchaseOrderRequest();
        createReq.setSupplierId(testSupplier.getId());
        createReq.setItems(List.of(itemReq));

        PurchaseOrderResponse createdPo = purchaseOrderService.createPurchaseOrder(adminId, createReq);
        assertEquals("DRAFT", createdPo.getStatus(), "Phiếu nhập mới phải ở trạng thái DRAFT");

        // BƯỚC 2: NHẬN HÀNG TOÀN BỘ
        ReceivePurchaseOrderRequest receiveReq = new ReceivePurchaseOrderRequest();
        receiveReq.setIdempotencyKey(java.util.UUID.randomUUID().toString()); // <--- THÊM DÒNG NÀY
        receiveReq.setLines(List.of(new PurchaseStockReceipt(testVariant.getId(), 10)));

        PurchaseOrderResponse receivedPo = purchaseOrderService.receivePurchaseOrder(createdPo.getId(), adminId, receiveReq);

        // BƯỚC 3: KIỂM CHỨNG LIÊN MODULE
        assertEquals("RECEIVED", receivedPo.getStatus(), "Phiếu nhập phải chuyển sang RECEIVED");

        // 3.1: Kiểm tra Catalog (Tồn kho vật lý phải tăng lên 10)
        ProductVariant updatedVariant = productVariantRepository.findById(testVariant.getId()).orElseThrow();
        assertEquals(10, updatedVariant.getQuantity(), "Tồn kho trong Catalog phải được cộng thêm 10");

        // 3.2: Kiểm tra Inventory (Phải sinh ra dòng StockMovement với reference là ID của phiếu nhập)
        List<org.akira.laptopstore.inventory.entity.StockMovement> movements = stockMovementRepository.findByVariantIdOrderByCreatedAtDesc(testVariant.getId());

        // Lấy dòng biến động kho MỚI NHẤT của variant này
        StockMovement latestMovement = movements.get(0);

        assertEquals("PURCHASE_RECEIPT", latestMovement.getType(), "Loại biến động kho phải là Nhập hàng");
        assertEquals(10, latestMovement.getDelta(), "Số lượng biến động phải là 10");
        assertEquals(createdPo.getId().toString(), latestMovement.getReferenceId(), "ID tham chiếu phải khớp với ID phiếu nhập");
/*
*Mã lỗi / Thông báo:org.opentest4j.AssertionFailedError: Phải sinh ra 1 dòng StockMovement ==> expected: <1> but was: <3> tại file ProcurementIntegrationTest.java:110
*Nguyên nhân:
    - Test Integration (tương tác trực tiếp với Database).
    - Hàm DatabaseSeeder.java đã tự động chạy và insert 2 dòng StockMovement (hoặc các record có liên quan) vào DB hệt như môi trường thật.
    → Do đó, khi gọi purchaseOrderService.receivePurchaseOrder(...), nó sinh ra thêm 1 dòng nữa
    → Tổng cộng DB có 3 dòng StockMovement.
    - Trong khi đó, ở dòng 110 của ProcurementIntegrationTest, em lại đếm toàn bộ số dòng trong bảng (stockMovementRepository.count()) và kỳ vọng nó bằng 1.
    - Sự xung đột giữa Data mồi của Seeder và dữ liệu do Test sinh ra đã làm failed assertion này.
* Giải pháp:
    - Đừng dùng hàm .count() đếm toàn bộ bảng.
    - Query trực tiếp để tìm chính xác dòng StockMovement vừa được sinh ra bởi phiếu nhập (dựa trên variantId và referenceId).
*/


    }
}