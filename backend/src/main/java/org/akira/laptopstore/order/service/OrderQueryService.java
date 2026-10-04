package org.akira.laptopstore.order.service;

import java.math.BigDecimal;

public interface OrderQueryService {

    BigDecimal getOrderTotalAmount(Long orderId);
}