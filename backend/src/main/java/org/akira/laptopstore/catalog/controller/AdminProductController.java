package org.akira.laptopstore.catalog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.catalog.dto.request.CreateProductRequest;
import org.akira.laptopstore.catalog.dto.request.CreateVariantRequest;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.service.ProductService;
import org.akira.laptopstore.catalog.service.ProductVariantService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import org.akira.laptopstore.catalog.dto.request.UpdateProductRequest;
import org.akira.laptopstore.catalog.dto.request.UpdateVariantRequest;
import org.akira.laptopstore.catalog.dto.response.ProductResponse;
import org.akira.laptopstore.catalog.dto.response.VariantSnapshot;
import org.akira.laptopstore.catalog.mapper.ProductMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // <-- THÊM DÒNG NÀY ĐỂ BẢO VỆ TOÀN BỘ CLASS
/*
======================== ĐÃ XÓA CODE CŨ VÀ CẬP NHẬP CODE MỚI - Annotation @PreAuthorize =======================
*Trong Spring Security, việc cấu hình chặn URL ở SecurityConfig (như requestMatchers("/api/v1/admin/**").hasRole("ADMIN")) là lớp khiên thứ nhất.
*Lớp khiên thứ hai là bảo vệ trực tiếp trên hàm/class bằng @PreAuthorize (Method Security).
Đã gắn @PreAuthorize("hasRole('ADMIN')") ở các hàm như updateProduct, createVariant... Rất tốt!
→ NHƯNG lại quên gắn ở createProduct, getAllProducts, toggleProductStatus.
→ Nếu sau này có ai đó lỡ tay sửa URL trong SecurityConfig, các API tạo sản phẩm này sẽ bị hổng toàn bộ.
    + Kéo lên đầu file, thêm @PreAuthorize("hasRole('ADMIN')") ngay trước khai báo lớp `public class AdminProductController`
    + Xóa hết các dòng @PreAuthorize("hasRole('ADMIN')") đang nằm lẻ tẻ ở các hàm bên dưới để code sạch đẹp hơn.
*/
public class AdminProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    private final ProductVariantService productVariantService;

    // 1. API Tạo mới sản phẩm (Có kiểm tra Validation)
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // 2. API Lấy danh sách tất cả sản phẩm
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.findAllProducts();
        return ResponseEntity.ok(products);
    }

    // 3. API Lấy chi tiết sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.findProductById(id);
        return ResponseEntity.ok(product);
    }

    // 4. API Activate / Deactivate sản phẩm (Thay đổi trạng thái)
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> toggleProductStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        productService.toggleProductStatus(id, status);
        return ResponseEntity.ok("Đã cập nhật trạng thái sản phẩm thành công!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        // Gọi xuống tầng Service mà em đã viết xong lúc nãy
        Product updatedProduct = productService.updateProduct(id, request);

        // Map kết quả trả về từ Entity sang Response DTO
        ProductResponse response = productMapper.toResponse(updatedProduct);

        // Trả về mã 200 OK cùng dữ liệu
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<VariantSnapshot> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantRequest request) {

        // Gọi Service xử lý
        VariantSnapshot response = productVariantService.createVariant(productId, request);

        // Trả về mã 201 CREATED thể hiện việc tạo mới thành công
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<VariantSnapshot> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantRequest request) {

        VariantSnapshot response = productVariantService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/variants/{variantId}/status")
    public ResponseEntity<String> updateVariantStatus(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestParam boolean active) {

        // Gọi Service xử lý việc bật/tắt (nhớ gọi đúng tên biến service của em, có thể là productVariantService hoặc productService)
        productVariantService.updateVariantActiveStatus(productId, variantId, active);

        // Tùy biến câu thông báo cho xịn xò
        String message = active ? "Kích hoạt biến thể thành công!" : "Hủy kích hoạt (Ngừng kinh doanh) biến thể thành công!";

        // Trả về mã 200 OK kèm lời nhắn
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<VariantSnapshot>> getVariantsByProductId(@PathVariable Long productId) {

        List<VariantSnapshot> response = productVariantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<VariantSnapshot> getVariantById(
            @PathVariable Long productId,
            @PathVariable Long variantId) {

        VariantSnapshot response = productVariantService.getVariantById(productId, variantId);
        return ResponseEntity.ok(response);
    }
}