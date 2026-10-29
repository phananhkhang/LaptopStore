package org.akira.laptopstore.order.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor

public class OrderResponse {
    public OrderResponse(Long id, Long userId, String orderCode, OrderStatus status,
            BigDecimal subtotal, BigDecimal discountAmount, BigDecimal shippingFee, BigDecimal totalAmount,
            String recipientName, String recipientPhone, String shippingAddress, String couponCodeSnapshot,
            List<OrderItemResponse> items, List<OrderStatusHistoryResponse> statusHistories, Instant createdAt, Instant updatedAt) {
        this.id=id; this.userId=userId; this.orderCode=orderCode; this.status=status;
        this.subtotal=subtotal; this.discountAmount=discountAmount; this.shippingFee=shippingFee; this.totalAmount=totalAmount;
        this.recipientName=recipientName; this.recipientPhone=recipientPhone; this.shippingAddress=shippingAddress;
        this.couponCodeSnapshot=couponCodeSnapshot; this.items=items; this.statusHistories=statusHistories;
        this.createdAt=createdAt; this.updatedAt=updatedAt;
    }
    private Long id;
    private Long userId;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String couponCodeSnapshot;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistories;
    private Instant createdAt;
    private Instant updatedAt;

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String value) { paymentMethod = value; }
    private String paymentMethod;
    private String customerNote;
}
