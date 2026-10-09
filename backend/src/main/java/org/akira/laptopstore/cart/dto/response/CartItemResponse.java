package org.akira.laptopstore.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;          // ID dòng giỏ hàng dùng cho PATCH/DELETE
    private Long variantId;          // ID của sản phẩm
    private String productName;
    private String sku;
    private String variantName;
    private Integer quantity;        // Số lượng sản phẩm trong giỏ
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
