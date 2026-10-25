package org.akira.laptopstore.order.scheduler;
 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akira.laptopstore.order.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationScheduler {

    private final OrderService orderService;
    /**
     * Chạy định kỳ (mặc định mỗi 60 giây) để quét và hủy các đơn hàng chưa thanh toán quá thời hạn.
     * Cách viết cũ - viết sai cú pháp lồng nhau: @Scheduled(fixedRateString = "${app.order.expire-check-interval-ms:${app.order.expire-check-interval-ms:60000}}") // LỖI
     * Cách sửa: Chuẩn hóa lại cú pháp (chỉ dùng 1 dấu : để set giá trị mặc định fallback):
     * Xóa bỏ sự lồng ghép, chỉ giữ lại một lớp phân giải mặc định duy nhất
    */
    @Scheduled(fixedDelayString = "${app.order.expire-check-interval-ms:60000}")
    public void scanAndExpirePendingOrders() {
        int expiredCount = orderService.expirePendingOrders();
        if (expiredCount > 0) {
            log.info("Auto-cancelled {} expired pending orders", expiredCount);
        }
    }
}
