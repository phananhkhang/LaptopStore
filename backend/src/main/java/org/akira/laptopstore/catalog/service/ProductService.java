package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> findAllProducts();
    Product findProductById(Long id);
    Product createProduct(Product product);
}