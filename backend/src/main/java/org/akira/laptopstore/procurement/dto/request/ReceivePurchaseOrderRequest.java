package org.akira.laptopstore.procurement.dto.request;

import org.akira.laptopstore.inventory.dto.request.PurchaseStockReceipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivePurchaseOrderRequest {
    @NotEmpty(message = "Danh sách mặt hàng nhận không được để trống")
    @Valid
    private List<PurchaseStockReceipt> lines;
}
