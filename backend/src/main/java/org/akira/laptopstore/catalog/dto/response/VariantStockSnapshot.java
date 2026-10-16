package org.akira.laptopstore.catalog.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Internal contract DTO cung cấp thông tin biến thể và tồn kho hiện tại
 * từ module catalog sang module inventory / procurement.
 * Không dùng để trả trực tiếp ra public HTTP API của catalog.
 */
@Value
@Builder
public class VariantStockSnapshot {
    Long variantId, productId;
    String productName, variantName, sku, ram, storage, color, status;
    BigDecimal price;
    Integer quantity;
    boolean active;
}
