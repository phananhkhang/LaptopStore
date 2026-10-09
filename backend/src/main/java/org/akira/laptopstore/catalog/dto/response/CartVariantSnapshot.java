package org.akira.laptopstore.catalog.dto.response;

import lombok.Value;
import java.math.BigDecimal;

/** Catalog data for cart display and validation, without exposing persistence entities. */
@Value
public class CartVariantSnapshot {
    Long variantId;
    String productName;
    String variantName;
    String sku;
    BigDecimal unitPrice;
    Integer quantity;
    boolean sellable;
}
