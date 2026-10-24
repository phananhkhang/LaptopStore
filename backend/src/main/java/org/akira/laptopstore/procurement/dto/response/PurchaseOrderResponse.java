package org.akira.laptopstore.procurement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id, createdByUserId;
    private String status, note;
    private LocalDateTime createdAt, receivedAt;
    private SupplierResponse supplier;
    private List<PurchaseOrderItemResponse> items;
}
