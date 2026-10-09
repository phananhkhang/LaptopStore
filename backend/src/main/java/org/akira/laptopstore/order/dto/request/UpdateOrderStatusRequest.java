package org.akira.laptopstore.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
