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
    public OrderSummaryResponse(Long id, String orderCode, OrderStatus status, int totalItems,
            BigDecimal totalAmount, String recipientName, Instant createdAt) {
        this.id=id; this.orderCode=orderCode; this.status=status; this.totalItems=totalItems;
        this.totalAmount=totalAmount; this.recipientName=recipientName; this.createdAt=createdAt;
    }
    private java.util.List<OrderItemResponse> items = java.util.List.of();
    private String paymentMethod;
    private Long id;
    private String orderCode;
    private OrderStatus status;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private String recipientName;
    private Instant createdAt;
}
