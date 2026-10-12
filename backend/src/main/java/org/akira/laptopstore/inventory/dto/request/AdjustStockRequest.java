package org.akira.laptopstore.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class AdjustStockRequest {
    @NotNull(message = "Số lượng thay đổi không được để trống")
    private Integer delta; // Có thể âm (xuất kho) hoặc dương (nhập kho)

    @NotBlank(message = "Lý do thay đổi tồn kho không được để trống")
    private String reason;
}