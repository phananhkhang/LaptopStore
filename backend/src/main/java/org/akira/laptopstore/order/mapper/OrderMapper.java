package org.akira.laptopstore.order.mapper;

import org.akira.laptopstore.order.dto.response.OrderItemResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.dto.response.OrderStatusHistoryResponse;
import org.akira.laptopstore.order.entity.Order;
import org.akira.laptopstore.order.entity.OrderItem;
import org.akira.laptopstore.order.entity.OrderStatusHistory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order, List<OrderItem> items,
                                    List<OrderStatusHistory> statusHistories) {
        return new OrderResponse(
                order.getId(), order.getUserId(), order.getOrderCode(), order.getStatus(),
                order.getSubtotal(), order.getDiscountAmount(), order.getShippingFee(), order.getTotalAmount(),
                order.getRecipientName(), order.getRecipientPhone(), order.getShippingAddress(),
                order.getCouponCodeSnapshot(),
                items.stream().map(this::toItemResponse).toList(),
                statusHistories.stream().map(this::toStatusHistoryResponse).toList(),
                order.getCreatedAt(), order.getUpdatedAt()
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order, int totalItems) {
        return new OrderSummaryResponse(
                order.getId(), order.getOrderCode(), order.getStatus(), totalItems,
                order.getTotalAmount(), order.getRecipientName(), order.getCreatedAt()
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getId(), item.getProductId(), item.getVariantId(), item.getProductName(),
                item.getVariantName(), item.getSku(), item.getUnitPrice(), item.getQuantity(), lineTotal
        );
    }

    public OrderStatusHistoryResponse toStatusHistoryResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getId(), history.getFromStatus(), history.getToStatus(),
                history.getChangedAt(), history.getChangedByUserId()
        );
    }
}
