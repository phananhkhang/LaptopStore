package org.akira.laptopstore.catalog.dto.response;

import lombok.Value;

import java.math.BigDecimal;
// Đây là dữ liệu trả về từ module catalog cho module order khi checkout
@Value
public class CheckoutVariantSnapshot {
    Long variantId;
    Long productId;
    String productName;
    String variantName;
    String sku;
    BigDecimal unitPrice;
}
