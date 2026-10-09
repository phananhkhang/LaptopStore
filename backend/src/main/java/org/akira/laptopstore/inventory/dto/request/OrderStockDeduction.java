package org.akira.laptopstore.inventory.dto.request;

import lombok.Value;

/** A single stock line requested by order checkout. */
@Value
public class OrderStockDeduction {
    Long variantId;
    Integer quantity;
}
