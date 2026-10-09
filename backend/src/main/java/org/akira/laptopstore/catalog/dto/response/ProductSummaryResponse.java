package org.akira.laptopstore.catalog.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSummaryResponse {
    private Long id;
    private String name, brandName, categoryName, status, thumbnailImage;
    // Chỉ lấy 1 URL ảnh duy nhất (ảnh có isPrimary = true)
    // Có thể mở rộng thêm logic lấy giá thấp nhất từ các Variant nếu cần
    // private BigDecimal startingPrice;
}