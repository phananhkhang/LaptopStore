package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.response.CheckoutVariantSnapshot;
import org.akira.laptopstore.catalog.dto.response.StockChangeResult;

import java.util.Collection;
import java.util.List;

/** Internal contract for checkout; it is deliberately not exposed by a controller. */
public interface CatalogCheckoutService {
    List<CheckoutVariantSnapshot> lockAndGetSellableVariants(Collection<Long> variantIds);

    StockChangeResult deductStock(Long variantId, int quantity);
}
