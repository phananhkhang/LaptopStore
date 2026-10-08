package org.akira.laptopstore.catalog.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class VariantSnapshot {
    private Long id;
    private String sku, ram, storage, color, status;
    private BigDecimal price;
    private Integer quantity;
}