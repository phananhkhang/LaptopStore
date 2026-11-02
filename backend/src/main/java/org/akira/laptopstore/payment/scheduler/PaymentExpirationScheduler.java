package org.akira.laptopstore.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.akira.laptopstore.payment.service.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationScheduler {

    private final PaymentService paymentService;

    /**
     * Mỗi 60 giây quét Payment đã hết hạn.
     */
    @Scheduled(fixedDelayString = "${app.payment.expire-check-interval-ms:60000}")
    public void scanAndExpirePendingPayments() {

        int expiredCount = paymentService.expirePendingPayments();

        if (expiredCount > 0) {

            log.info(
                    "Marked {} expired payments as FAILED",
                    expiredCount);
        }
    }
}