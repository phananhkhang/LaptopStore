package org.akira.laptopstore.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    public CartItemResponse(Long cartItemId, Long variantId, String productName, String sku, String variantName,
            Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        this.cartItemId=cartItemId; this.variantId=variantId; this.productName=productName; this.sku=sku;
        this.variantName=variantName; this.quantity=quantity; this.unitPrice=unitPrice; this.lineTotal=lineTotal;
    }
    private Long productId;
    private Integer availableQuantity;
    private Boolean sellable;
    private Long cartItemId;          // ID dòng giỏ hàng dùng cho PATCH/DELETE
    private Long variantId;          // ID của sản phẩm
    private String productName;
    private String sku;
    private String variantName;
    private Integer quantity;        // Số lượng sản phẩm trong giỏ
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
