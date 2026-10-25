package org.akira.laptopstore.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.akira.laptopstore.order.entity.enums.OrderStatus;

@Getter
@Builder
public class OrderItemReviewInfo {

    private Long orderItemId;

    private Long orderId;

    private Long userId;

    private Long productId;

    private Long variantId;

    // Snapshot tại thời điểm mua, dùng để hiển thị "Đã mua: ..." dưới review
    private String productName;

    private String variantName;

    private OrderStatus orderStatus;
}
