package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.request.CreateProductRequest;
import org.akira.laptopstore.catalog.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> findAllProducts();

    Product findProductById(Long id);

    // Đổi tham số từ Product sang CreateProductRequest
    Product createProduct(CreateProductRequest request);

    // Khai báo thêm hàm thay đổi trạng thái
    void toggleProductStatus(Long productId, String status);
}