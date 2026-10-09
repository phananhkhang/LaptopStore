package org.akira.laptopstore.catalog.dto.response;

import lombok.Value;

import java.math.BigDecimal;

/** Immutable catalog data used by the order checkout transaction. */
@Value
public class CheckoutVariantSnapshot {
    Long variantId;
    Long productId;
    String productName;
    String variantName;
    String sku;
    BigDecimal unitPrice;
}
