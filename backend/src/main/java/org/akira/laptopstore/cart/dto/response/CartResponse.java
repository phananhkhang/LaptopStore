package org.akira.laptopstore.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,                         // ID của giỏ hàng
        List<CartItemResponse> items,    // Danh sách các món hàng
        Integer totalItems,              // Tổng số lượng món (tổng quantity của các item)
        BigDecimal totalAmount           // Tổng tiền tạm tính cả giỏ hàng (tổng các lineTotal)
) {
}
