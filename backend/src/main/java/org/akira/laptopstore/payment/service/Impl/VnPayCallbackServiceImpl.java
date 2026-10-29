package org.akira.laptopstore.payment.service.Impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.akira.laptopstore.payment.dto.response.VnPayIpnResponse;
import org.akira.laptopstore.payment.dto.response.VnPayReturnResponse;
import org.akira.laptopstore.payment.entity.Payment;
import org.akira.laptopstore.payment.entity.PaymentTransaction;
import org.akira.laptopstore.payment.enums.PaymentStatus;
import org.akira.laptopstore.payment.enums.PaymentTransactionStatus;
import org.akira.laptopstore.payment.integration.VnPayGateway;
import org.akira.laptopstore.payment.repository.PaymentRepository;
import org.akira.laptopstore.payment.repository.PaymentTransactionRepository;
import org.akira.laptopstore.payment.service.VnPayCallbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VnPayCallbackServiceImpl
        implements VnPayCallbackService {

    private final VnPayGateway vnPayGateway;
    private final org.akira.laptopstore.order.service.OrderPaymentService orderPaymentService;

    private final PaymentRepository paymentRepository;

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Override
    @Transactional
    public VnPayIpnResponse handleIpn(
            Map<String, String> params) {

        // Bước 1: kiểm tra chữ ký VNPay
        if (!vnPayGateway.verifySignature(params)) {

            return VnPayIpnResponse
                    .invalidSignature();
        }

        // Bước 2: kiểm tra đúng TMN Code của merchant
        if (!vnPayGateway.isExpectedTmnCode(
                params.get("vnp_TmnCode"))) {

            return VnPayIpnResponse
                    .invalidSignature();
        }

        // Bước 3: lấy TxnRef
        String transactionRef = params.get("vnp_TxnRef");

        if (!StringUtils.hasText(
                transactionRef)) {

            return VnPayIpnResponse
                    .notFound();
        }

        // Bước 4: tìm transaction của hệ thống mình
        PaymentTransaction transaction = paymentTransactionRepository
                .findByProviderReferenceForUpdate(
                        transactionRef)
                .orElse(null);

        if (transaction == null) {

            return VnPayIpnResponse
                    .notFound();
        }

        // Bước 5: lấy payment tương ứng
        Payment payment = paymentRepository
                .findByIdForUpdate(
                        transaction
                                .getPayment()
                                .getId())
                .orElse(null);

        if (payment == null) {

            return VnPayIpnResponse
                    .notFound();
        }

        // Bước 6: kiểm tra amount
        BigDecimal callbackAmount = parseVnpAmount(
                params.get("vnp_Amount"));

        if (callbackAmount == null
                || payment
                        .getAmount()
                        .compareTo(callbackAmount) != 0) {

            return VnPayIpnResponse
                    .invalidAmount();
        }

        // Bước 7: transaction đã xử lý rồi
        if (transaction.getStatus() != PaymentTransactionStatus.PENDING) {

            return VnPayIpnResponse
                    .alreadyProcessed();
        }

        // Bước 8: lấy mã giao dịch VNPay
        Long providerTransactionId = parseLong(
                params.get(
                        "vnp_TransactionNo"));

        if (providerTransactionId != null) {

            transaction
                    .setProviderTransactionId(
                            providerTransactionId);
        }

        // Bước 9: kiểm tra giao dịch thành công
        boolean success = "00".equals(
                params.get(
                        "vnp_ResponseCode"))
                &&
                "00".equals(
                        params.get(
                                "vnp_TransactionStatus"));

        if (success) {

            transaction.setStatus(
                    PaymentTransactionStatus.SUCCESS);

            payment.setStatus(
                    orderPaymentService.recordVerifiedPayment(payment.getOrderId())
                            ? PaymentStatus.PAID : PaymentStatus.REQUIRES_RECONCILIATION);

            payment.setUpdatedAt(
                    Instant.now());

        } else {

            transaction.setStatus(
                    PaymentTransactionStatus.FAILED);

            if (payment.getStatus() != PaymentStatus.PAID && payment.getStatus() != PaymentStatus.REQUIRES_RECONCILIATION) {

                payment.setStatus(
                        PaymentStatus.FAILED);

                payment.setUpdatedAt(
                        Instant.now());
            }
        }

        // Bước 10: lưu DB
        paymentTransactionRepository
                .save(transaction);

        paymentRepository
                .save(payment);

        return VnPayIpnResponse
                .success();
    }

    @Override
    @Transactional(readOnly = true)
    public VnPayReturnResponse inspectReturn(
            Map<String, String> params) {

        boolean signatureValid = vnPayGateway
                .verifySignature(params)
                &&
                vnPayGateway
                        .isExpectedTmnCode(
                                params.get(
                                        "vnp_TmnCode"));

        boolean successful = signatureValid
                &&
                "00".equals(
                        params.get(
                                "vnp_ResponseCode"))
                &&
                "00".equals(
                        params.get(
                                "vnp_TransactionStatus"));

        return VnPayReturnResponse
                .builder()
                .signatureValid(
                        signatureValid)
                .successful(
                        successful)
                .transactionRef(
                        params.get(
                                "vnp_TxnRef"))
                .responseCode(
                        params.get(
                                "vnp_ResponseCode"))
                .transactionStatus(
                        params.get(
                                "vnp_TransactionStatus"))
                .transactionNo(
                        params.get(
                                "vnp_TransactionNo"))
                .message(
                        successful
                                ? "Payment successful"
                                : "Payment was not confirmed")
                .build();
    }

    private BigDecimal parseVnpAmount(
            String rawAmount) {

        try {

            if (!StringUtils.hasText(
                    rawAmount)) {

                return null;
            }

            return new BigDecimal(
                    rawAmount)
                    .movePointLeft(2);

        } catch (NumberFormatException ex) {

            return null;
        }
    }

    private Long parseLong(
            String value) {

        try {

            return StringUtils.hasText(
                    value)
                            ? Long.valueOf(value)
                            : null;

        } catch (NumberFormatException ex) {

            return null;
        }
    }
}
