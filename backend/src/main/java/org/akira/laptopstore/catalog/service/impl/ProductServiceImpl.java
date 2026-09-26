package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.repository.ProductRepository;
import org.akira.laptopstore.catalog.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
}