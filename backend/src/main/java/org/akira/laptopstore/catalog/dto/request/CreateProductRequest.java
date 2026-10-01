package org.akira.laptopstore.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

        @NotBlank(message = "Tên sản phẩm không được để trống")
        private String name;

        @NotBlank(message = "Mô tả sản phẩm không được để trống")
        private String description;

        @NotBlank(message = "SKU không được để trống")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU chỉ được chứa chữ in hoa, số và dấu gạch ngang (-)")
        private String sku;

        @NotNull(message = "Giá sản phẩm không được để trống")
        @Min(value = 1, message = "Giá sản phẩm phải lớn hơn 0")
        private BigDecimal price;

        @NotNull(message = "ID của danh mục (Category) không được để trống")
        private Long categoryId;

        @NotNull(message = "ID của thương hiệu (Brand) không được để trống")
        private Long brandId;
}