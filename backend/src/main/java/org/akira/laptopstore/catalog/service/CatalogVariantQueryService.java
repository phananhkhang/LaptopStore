package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.response.VariantStockSnapshot;

import java.util.Collection;
import java.util.List;

/**
 * Internal query contract cung cấp thông tin biến thể và tồn kho hiện tại
 * cho module inventory và procurement.
 */
public interface CatalogVariantQueryService
{
    VariantStockSnapshot getVariantStock(Long variantId);

    List<VariantStockSnapshot> getVariantStocks(Collection<Long> variantIds);

    List<VariantStockSnapshot> getAllVariantStocks();

    List<VariantStockSnapshot> getLowStockVariants(int threshold);
}
