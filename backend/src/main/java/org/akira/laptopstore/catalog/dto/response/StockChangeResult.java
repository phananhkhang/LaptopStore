package org.akira.laptopstore.catalog.dto.response;

import lombok.Value;

/** Result of a stock mutation. Only inventory is allowed to invoke the mutation contract. */
@Value
public class StockChangeResult {
    Long variantId;
    int quantityBefore;
    int quantityAfter;
}
