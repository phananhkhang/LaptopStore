package org.akira.laptopstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponse {
    private Long id, variantId, createdByUserId;
    private String sku, productName, variantName, type, referenceType, referenceId, operationKey, note;
    private Integer delta, quantityBefore, quantityAfter;
    private LocalDateTime createdAt;
}
