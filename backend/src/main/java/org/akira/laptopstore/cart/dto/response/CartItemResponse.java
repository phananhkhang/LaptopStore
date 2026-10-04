package org.akira.laptopstore.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,          // ID dòng giỏ hàng dùng cho PATCH/DELETE
        Long variantId,          // ID của sản phẩm
        String productName,      // Tạm thời null, chờ contract catalog
        String sku,              // Tạm thời null, chờ contract catalog
        String variantName,      // Tạm thời null, chờ contract catalog
        Integer quantity,        // Số lượng sản phẩm trong giỏ
        BigDecimal unitPrice,    // Tạm thời null, chờ contract catalog
        BigDecimal lineTotal     // Tạm thời null, chờ contract catalog
) {
}
