package org.akira.laptopstore.catalog.controller;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.catalog.dto.request.ProductSearchCriteria;
import org.akira.laptopstore.catalog.dto.response.ProductSummaryResponse;

import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.service.ProductService;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductSummaryResponse>> searchProducts(
        // @ModelAttribute tự động "hút" các query param trên URL (ví dụ: ?keyword=asus&minPrice=1000) 
        // và nhồi thẳng vào các thuộc tính tương ứng của object ProductSearchCriteria.
        @ModelAttribute ProductSearchCriteria criteria,

        // @PageableDefault bọc sẵn các tham số (page=0, size=12, sort=createdAt,desc)
        // Frontend có thể ghi đè bằng cách truyền: ?page=1&size=24&sort=price,asc
        @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ProductSummaryResponse> result = productService.searchAndFilterProducts(criteria, pageable);
        return ResponseEntity.ok(result);
    }
}