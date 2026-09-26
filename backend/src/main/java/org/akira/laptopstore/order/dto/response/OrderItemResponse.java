package org.akira.laptopstore.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        Long variantId,
        String productName,
        String variantName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
