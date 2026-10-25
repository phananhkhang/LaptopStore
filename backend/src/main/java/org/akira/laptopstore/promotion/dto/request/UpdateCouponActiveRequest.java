package org.akira.laptopstore.promotion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCouponActiveRequest {

    @NotNull
    private Boolean active;
}
