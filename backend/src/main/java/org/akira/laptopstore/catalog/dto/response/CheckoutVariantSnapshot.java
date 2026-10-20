package org.akira.laptopstore.catalog.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Snapshot dữ liệu biến thể trả về từ module catalog cho module order khi checkout.
 */
@Getter
@ToString
@EqualsAndHashCode
public final class CheckoutVariantSnapshot {
    private final Long variantId;
    private final Long productId;
    private final String productName;
    private final String variantName;
    private final String sku;
    private final BigDecimal unitPrice;
    private final Integer availableQuantity;

    public CheckoutVariantSnapshot(
            Long variantId,
            Long productId,
            String productName,
            String variantName,
            String sku,
            BigDecimal unitPrice
    ) {
        this(variantId, productId, productName, variantName, sku, unitPrice, null);
    }

    public CheckoutVariantSnapshot(
            Long variantId,
            Long productId,
            String productName,
            String variantName,
            String sku,
            BigDecimal unitPrice,
            Integer availableQuantity
    ) {
        this.variantId = variantId;
        this.productId = productId;
        this.productName = productName;
        this.variantName = variantName;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.availableQuantity = availableQuantity;
    }
}
