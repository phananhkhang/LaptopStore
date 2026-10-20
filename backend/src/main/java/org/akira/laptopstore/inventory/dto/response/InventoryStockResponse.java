package org.akira.laptopstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockResponse {
    private Long variantId, productId;
    private String productName, variantName, sku, ram, storage, color, status;
    private BigDecimal price;
    private Integer quantity;
    private boolean active, lowStock;
}
