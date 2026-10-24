package org.akira.laptopstore.inventory.dto.request;

import lombok.Value;

/**
 * DTO nội bộ nhận thông tin dòng hàng từ module procurement
 * chuyển sang inventory để thực hiện cộng tồn kho.
 */
@Value
public class PurchaseStockReceipt {
    Long variantId;
    Integer quantity;
}
