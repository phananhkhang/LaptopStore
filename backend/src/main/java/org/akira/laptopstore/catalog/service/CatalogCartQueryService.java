package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.response.CartVariantSnapshot;
import java.util.Collection;
import java.util.List;

public interface CatalogCartQueryService {
    CartVariantSnapshot getVariant(Long variantId);
    List<CartVariantSnapshot> getVariants(Collection<Long> variantIds);
}
