package org.akira.laptopstore.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long variantId,          // ID của sản phẩm
        String productName,      // Tên sản phẩm
        String sku,
        String variantName,
        Integer quantity,        // Số lượng sản phẩm trong giỏ
        BigDecimal unitPrice,    // Giá đơn vị của sản phẩm
        BigDecimal lineTotal     // Tổng tiền cho dòng sản phẩm (quantity * unitPrice)
) {
}
