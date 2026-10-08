package org.akira.laptopstore.catalog.service.impl;

// 1. Import các thư viện của Spring Framework và Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

// 2. Import các Entity và Repository của em
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.entity.ProductVariant;
import org.akira.laptopstore.catalog.repository.ProductRepository;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.catalog.service.ProductVariantService;

import java.util.List;
import java.util.stream.Collectors;

// 3. Import các DTO và Mapper
import org.akira.laptopstore.catalog.dto.request.CreateVariantRequest;
import org.akira.laptopstore.catalog.dto.request.UpdateVariantRequest;
import org.akira.laptopstore.catalog.dto.response.VariantSnapshot;
import org.akira.laptopstore.catalog.mapper.ProductMapper;

// 4. Import các Exception do dự án tự định nghĩa (em điều chỉnh package cho khớp nhé)
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Slf4j // Dùng để ghi log nếu cần
public class ProductVariantServiceImpl implements ProductVariantService {

    // Tiêm các dependency cần thiết vào
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository; // Cần cái này để tìm Product cha
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public VariantSnapshot createVariant(Long productId, CreateVariantRequest request) {

        // 1. Kiểm tra xem Product gốc có tồn tại không
        Product parentProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi: Không tìm thấy Product gốc với ID: " + productId));

        // 2. Kiểm tra mã SKU đã tồn tại chưa (Dựa vào hàm existsBySku em đã viết)
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "SKU_ALREADY_EXISTS",
                    "Mã SKU '" + request.getSku() + "' đã bị trùng. Vui lòng nhập mã khác!"
            );
        }

        // 3. Khởi tạo Variant mới
        ProductVariant newVariant = ProductVariant.builder()
                .product(parentProduct)
                .sku(request.getSku())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .status("ACTIVE") // hễ tạo mới là mặc định trạng thái hoạt động
                .build();

        // 4. Lưu xuống Database
        ProductVariant savedVariant = productVariantRepository.save(newVariant);

        // 5. Trả về kết quả qua Mapper
        return productMapper.toVariantSnapshot(savedVariant);
    }

    @Override
    @Transactional
    public VariantSnapshot updateVariant(Long productId, Long variantId, UpdateVariantRequest request) {
        // 1. Kiểm tra Variant có tồn tại không
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi: Không tìm thấy biến thể với ID: " + variantId));

        // 2. Chốt chặn bảo mật: Variant này có đúng là thuộc về Product cha (productId) không?
        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PRODUCT_VARIANT",
                    "Lỗi: Biến thể ID " + variantId + " không thuộc về sản phẩm ID " + productId
            );
        }

        // 3. Kiểm tra trùng SKU (chỉ kiểm tra khi SKU gửi lên khác SKU hiện tại)
        if (!variant.getSku().equalsIgnoreCase(request.getSku())) {
            if (productVariantRepository.existsBySkuAndIdNot(request.getSku(), variantId)) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "SKU_ALREADY_EXISTS",
                        "Mã SKU '" + request.getSku() + "' đã được sử dụng bởi biến thể khác!"
                );
            }
        }

        // 4. Cập nhật dữ liệu vào Entity (sử dụng Setter, không build mới để giữ nguyên ID & Audit fields)
        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setQuantity(request.getQuantity());
        variant.setStatus(request.getStatus());
        variant.setColor(request.getColor());
        variant.setRam(request.getRam());
        variant.setStorage(request.getStorage());

        // 5. Lưu lại xuống database
        ProductVariant updatedVariant = productVariantRepository.save(variant);

        // 6. Map và trả về kết quả
        return productMapper.toVariantSnapshot(updatedVariant);
    }

    @Override
    @Transactional
    public void updateVariantActiveStatus(Long productId, Long variantId, boolean isActive) {
// Gọi thẳng hàm Native Query để update
        int updatedRows = productVariantRepository.updateActiveStatus(productId, variantId, isActive);

        // Nếu không có dòng nào được cập nhật, nghĩa là sai ID Variant hoặc sai ID Product
        if (updatedRows == 0) {
            throw new ResourceNotFoundException(
                "Lỗi: Không tìm thấy Variant với ID: " + variantId + " thuộc về Product ID: " + productId
            );
        }
    }

    @Override
    public List<VariantSnapshot> getVariantsByProductId(Long productId) {
        // 1. Kiểm tra Product có tồn tại không
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Lỗi: Không tìm thấy Product với ID: " + productId);
        }

        // 2. Lấy danh sách Variant từ DB (đã tự động lọc active = true)
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);

        // 3. Map từ Entity sang DTO và trả về
        return variants.stream()
                .map(productMapper::toVariantSnapshot)
                .collect(Collectors.toList());
    }

    @Override
    public VariantSnapshot getVariantById(Long productId, Long variantId) {
        // 1. Tìm Variant theo ID
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi: Không tìm thấy Variant với ID: " + variantId));

        // 2. Kiểm tra chéo: Variant này có đúng là của Product này không?
        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "INVALID_PRODUCT_VARIANT", 
                "Lỗi: Biến thể không thuộc về sản phẩm có ID: " + productId
            );
        }

        // 3. Map sang DTO và trả về
        return productMapper.toVariantSnapshot(variant);
    }

}