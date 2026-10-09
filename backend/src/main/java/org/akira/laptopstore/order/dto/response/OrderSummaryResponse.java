package org.akira.laptopstore.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private Long id;
    private String orderCode;
    private OrderStatus status;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private String recipientName;
    private Instant createdAt;
}
