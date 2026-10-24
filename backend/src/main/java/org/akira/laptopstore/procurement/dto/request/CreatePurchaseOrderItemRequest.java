package org.akira.laptopstore.procurement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderItemRequest {
    @NotNull(message = "ID biến thể không được để trống")
    private Long variantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Đơn giá nhập không được để trống")
    @Min(value = 0, message = "Đơn giá nhập không được âm")
    private BigDecimal unitCost;
}
