package org.akira.laptopstore.order.service;

import java.math.BigDecimal;
import org.akira.laptopstore.order.entity.enums.CheckoutPaymentMethod;

/** Internal payment contract. Caller owns a transaction; no provider/network work here. */
public interface OrderPaymentService {
    BigDecimal lockPayableOrder(Long orderId, Long userId, CheckoutPaymentMethod method);
    BigDecimal lockDeliveredCodOrder(Long orderId);
    /** False means the captured payment must be reconciled; never revive a cancelled order. */
    boolean recordVerifiedPayment(Long orderId);
}
