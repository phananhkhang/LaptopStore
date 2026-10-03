package org.akira.laptopstore.review.fakeOrder;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FakeOrderService {

    private final Map<Long, OrderItemReviewInfo> orderItems = new HashMap<>();

    public FakeOrderService() {
        orderItems.put(101L, new OrderItemReviewInfo(101L, 1L, 10L, "DELIVERED"));

        orderItems.put(102L, new OrderItemReviewInfo(102L, 1L, 11L, "PROCESSING"));

        orderItems.put(201L, new OrderItemReviewInfo(201L, 2L, 12L, "DELIVERED"));
    }

    public OrderItemReviewInfo getOrderItem(Long orderItemId) {

        OrderItemReviewInfo item = orderItems.get(orderItemId);

        if (item == null) {
            throw new RuntimeException(
                    "Order item not found");
        }

        return item;
    }
}
