package org.akira.laptopstore.catalog.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSearchCriteria {
    // Tìm linh hoạt theo Tên sản phẩm HOẶC mã SKU
    private String keyword, brandName, categoryName, status, ram, storage, sortPriceDirection;
    // sortPriceDirection → Hứng hướng sort ("asc" hoặc "desc") từ Frontend
    private BigDecimal minPrice, maxPrice;
}
