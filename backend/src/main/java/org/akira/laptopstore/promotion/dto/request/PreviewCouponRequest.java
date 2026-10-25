package org.akira.laptopstore.promotion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Xem trước coupon. subtotal do client gửi chỉ mang tính tham khảo;
 * khi đặt hàng, order tự tính lại subtotal trên server rồi mới reserve.
 */
@Getter
@Setter
public class PreviewCouponRequest {

    @NotBlank
    private String code;

    @NotNull
    @Positive
    private BigDecimal subtotal;
}
