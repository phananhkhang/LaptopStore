package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.dto.request.CreateProductRequest;

import org.akira.laptopstore.catalog.entity.Brand;
import org.akira.laptopstore.catalog.entity.Category;
import org.akira.laptopstore.catalog.entity.Product;

import org.akira.laptopstore.catalog.repository.BrandRepository;
import org.akira.laptopstore.catalog.repository.CategoryRepository;
import org.akira.laptopstore.catalog.repository.ProductRepository;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;
import org.akira.laptopstore.catalog.repository.ProductSpecs;

import org.akira.laptopstore.catalog.service.ProductService;

import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.akira.laptopstore.catalog.dto.response.ProductSummaryResponse;
import org.akira.laptopstore.catalog.dto.request.ProductSearchCriteria;
import org.akira.laptopstore.catalog.dto.request.UpdateProductRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    // Thêm dòng này để "tiêm" ProductVariantRepository vào
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        // Đổi hàm kiểm tra sang repository của Variant
        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new BusinessException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "SKU_ALREADY_EXISTS", 
                "Mã SKU '" + request.getSku() + "' đã tồn tại trong hệ thống. Vui lòng nhập mã khác!"
            );
        }

        // 1. Chặn đứng lỗi Foreign Key ngay tại đây!
        validateBrandAndCategoryExists(request.getBrandId(), request.getCategoryId());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với ID: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand với ID: " + request.getBrandId()));

        // Mapping dữ liệu từ DTO sang Entity. Không map sku và price ở đây vì nó của Variant
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .brand(brand)
                .status("ACTIVE") // Lưu dưới dạng String theo chuẩn của Entity
                .build();

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long productId, UpdateProductRequest request) {
        // 1. Tìm sản phẩm cần cập nhật trong Database trước
        // Nếu truyền bậy bạ một cái ID không có, chặn đứng và quăng lỗi ngay!
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với ID: " + productId));

        // 2. Chặn đứng lỗi Foreign Key cho Brand và Category mới được truyền vào
        validateBrandAndCategoryExists(request.getBrandId(), request.getCategoryId());

        // 3. Lấy Entity Category và Brand thực tế từ Database dựa vào ID mới
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với ID: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand với ID: " + request.getBrandId()));

        // 4. Cập nhật các trường dữ liệu mới cho Entity cũ
        // Lưu ý: Chỉ set những trường thuộc về Product, tuyệt đối không đụng SKU/Price ở đây.
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setCategory(category);
        existingProduct.setBrand(brand);
        
        // Nếu DTO của em (UpdateProductRequest) có cho phép cập nhật thêm trạng thái, em có thể mở comment dòng dưới:
        // if (request.getStatus() != null) {
        //     existingProduct.setStatus(request.getStatus());
        // }

        // 5. Lưu đè thông tin mới xuống Database và trả về
        return productRepository.save(existingProduct);
    }

    // NGHIỆP VỤ ACTIVATE / DEACTIVATE
    @Override
    @Transactional
    public void toggleProductStatus(Long productId, String status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với ID: " + productId));

        // Cập nhật trạng thái
        product.setStatus(status);

        productRepository.save(product);
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Product với ID: " + id));
    }

    @Override
    public Page<ProductSummaryResponse> searchAndFilterProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecs.filterBy(criteria);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        // Tận dụng MapStruct hoặc map() của Page để chuyển đổi Entity sang DTO
        return productPage.map(product -> {
            ProductSummaryResponse dto = new ProductSummaryResponse();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setStatus(product.getStatus());
            // Giả sử có mapper, gọi: return productMapper.toProductSummaryResponse(product);
            return dto;
        });
    }

    private void validateBrandAndCategoryExists(Long brandId, Long categoryId) {
        // Kiểm tra Brand
        if (brandId != null && !brandRepository.existsById(brandId)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "BRAND_NOT_FOUND",
                "Lỗi: Thương hiệu (Brand) với ID " + brandId + " không tồn tại trong hệ thống!"
            );
        }

        // Kiểm tra Category
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new BusinessException(
                HttpStatus.BAD_REQUEST,
                "CATEGORY_NOT_FOUND",
                "Lỗi: Danh mục (Category) với ID " + categoryId + " không tồn tại trong hệ thống!"
            );
        }
    }
}