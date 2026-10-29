package org.akira.laptopstore.catalog.dto.response;

import lombok.Value;
import java.math.BigDecimal;

/** Catalog data for cart display and validation, without exposing persistence entities. */
@lombok.AllArgsConstructor
@Value
public class CartVariantSnapshot {
    public CartVariantSnapshot(Long variantId, String productName, String variantName, String sku,
            BigDecimal unitPrice, Integer quantity, boolean sellable) {
        this(null, variantId, productName, variantName, sku, unitPrice, quantity, sellable);
    }
    Long productId;
    Long variantId;
    String productName;
    String variantName;
    String sku;
    BigDecimal unitPrice;
    Integer quantity;
    boolean sellable;
}
