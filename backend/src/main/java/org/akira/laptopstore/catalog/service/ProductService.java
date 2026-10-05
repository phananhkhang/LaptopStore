package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.request.CreateProductRequest;
import org.akira.laptopstore.catalog.dto.request.CreateVariantRequest;
import org.akira.laptopstore.catalog.dto.request.UpdateProductRequest;

import org.akira.laptopstore.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.akira.laptopstore.catalog.dto.response.ProductSummaryResponse;
import org.akira.laptopstore.catalog.dto.request.ProductSearchCriteria;

import java.util.List;

public interface ProductService {
    List<Product> findAllProducts();

    Product findProductById(Long id);

    // Đổi tham số từ Product sang CreateProductRequest
    Product createProduct(CreateProductRequest request);

    Product updateProduct(Long id, UpdateProductRequest request);

    // Khai báo thêm hàm thay đổi trạng thái
    void toggleProductStatus(Long productId, String status);

    Page<ProductSummaryResponse> searchAndFilterProducts(ProductSearchCriteria criteria, Pageable pageable);
}