package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.dto.request.CreateProductRequest;
import org.akira.laptopstore.catalog.entity.Brand;
import org.akira.laptopstore.catalog.entity.Category;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.repository.BrandRepository;
import org.akira.laptopstore.catalog.repository.CategoryRepository;
import org.akira.laptopstore.catalog.repository.ProductRepository;
import org.akira.laptopstore.catalog.service.ProductService;
import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request) {
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
}