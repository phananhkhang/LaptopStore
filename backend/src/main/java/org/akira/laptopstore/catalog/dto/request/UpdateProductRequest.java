package org.akira.laptopstore.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @NotNull(message = "ID sản phẩm không được để trống khi cập nhật")
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Min(value = 1, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Trạng thái kích hoạt (isActive) không được để trống")
    private Boolean isActive;

    @NotNull(message = "ID của danh mục (Category) không được để trống")
    private Long categoryId;

    @NotNull(message = "ID của thương hiệu (Brand) không được để trống")
    private Long brandId;
}