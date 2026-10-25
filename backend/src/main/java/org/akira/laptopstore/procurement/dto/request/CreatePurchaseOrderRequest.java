package org.akira.laptopstore.procurement.dto.request;

import jakarta.validation.Valid;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {
    @NotNull(message = "ID nhà cung cấp không được để trống")
    private Long supplierId;

    private String note;

    @NotEmpty(message = "Phiếu nhập phải có ít nhất một mặt hàng")
    @Valid
    private List<CreatePurchaseOrderItemRequest> items;
}
