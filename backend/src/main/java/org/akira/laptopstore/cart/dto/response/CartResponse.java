package org.akira.laptopstore.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;                         // ID của giỏ hàng
    private List<CartItemResponse> items;    // Danh sách các món hàng
    private Integer totalItems;              // Tổng số lượng món (tổng quantity của các item)
    private BigDecimal totalAmount;          // Tổng tiền tạm tính cả giỏ hàng (tổng các lineTotal)
}
