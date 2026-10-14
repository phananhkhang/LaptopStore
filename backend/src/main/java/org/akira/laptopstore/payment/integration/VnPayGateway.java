package org.akira.laptopstore.payment.integration;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VnPayGateway {

    private static final DateTimeFormatter VNP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    public String createPaymentUrl(
            BigDecimal amount,
            String transactionRef,
            String clientIp,
            Instant expiresAt) {

        Instant now = Instant.now();

        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);

        params.put(
                "vnp_Amount",
                amount.multiply(BigDecimal.valueOf(100))
                        .toBigIntegerExact()
                        .toString()
        );

        params.put("vnp_CurrCode", "VND");

        params.put("vnp_TxnRef", transactionRef);

        params.put(
                "vnp_OrderInfo",
                "Thanh toan don hang " + transactionRef
        );

        params.put("vnp_OrderType", "other");

        params.put("vnp_Locale", "vn");

        params.put("vnp_ReturnUrl", returnUrl);

        params.put("vnp_IpAddr", clientIp);

        params.put(
                "vnp_CreateDate",
                VNP_DATE_FORMAT.format(now)
        );

        params.put(
                "vnp_ExpireDate",
                VNP_DATE_FORMAT.format(expiresAt)
        );

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {

            if (!hashData.isEmpty()) {
                hashData.append('&');
                query.append('&');
            }

            hashData
                    .append(entry.getKey())
                    .append('=')
                    .append(entry.getValue());

            query
                    .append(URLEncoder.encode(
                            entry.getKey(),
                            StandardCharsets.UTF_8
                    ))
                    .append('=')
                    .append(URLEncoder.encode(
                            entry.getValue(),
                            StandardCharsets.UTF_8
                    ));
        }

        String secureHash =
                hmacSha512(hashSecret, hashData.toString());

        return payUrl
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;
    }

    private String hmacSha512(
            String key,
            String data) {

        try {

            Mac hmac =
                    Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            key.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA512"
                    );

            hmac.init(secretKey);

            byte[] bytes =
                    hmac.doFinal(
                            data.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : bytes) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot create VNPay secure hash",
                    e
            );
        }
    }
}