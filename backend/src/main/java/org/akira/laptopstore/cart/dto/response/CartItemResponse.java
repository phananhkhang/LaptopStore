package org.akira.laptopstore.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,          // ID dòng giỏ hàng dùng cho PATCH/DELETE
        Long variantId,          // ID của sản phẩm
        String productName,
        String sku,
        String variantName,
        Integer quantity,        // Số lượng sản phẩm trong giỏ
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
