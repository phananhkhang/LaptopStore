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
public class UpdateVariantRequest {

    @NotBlank(message = "SKU của phiên bản không được để trống")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU phiên bản chỉ được chứa chữ in hoa, số và dấu gạch ngang (-)")
    private String sku;

    @NotBlank(message = "Tên phiên bản không được để trống")
    private String variantName;

    @NotNull(message = "Giá của phiên bản không được để trống")
    @Min(value = 1, message = "Giá phiên bản phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private Integer quantity;

    @NotBlank(message = "Trạng thái biến thể không được để trống")
    private String status;

    @NotBlank(message = "Màu sắc không được để trống")
    private String color;

    @NotBlank(message = "Dung lượng RAM không được để trống")
    private String ram;

    @NotBlank(message = "Dung lượng ổ cứng không được để trống")
    private String storage;
}