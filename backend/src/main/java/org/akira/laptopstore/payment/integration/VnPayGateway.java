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

import org.akira.laptopstore.payment.config.VnPayProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class VnPayGateway {

        private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

        private final VnPayProperties properties;

        public String createPaymentUrl(
                        BigDecimal amount,
                        String transactionRef,
                        String clientIp,
                        Instant expiresAt) {

                validateConfiguration();

                Instant now = Instant.now();

                Map<String, String> params = new TreeMap<>();

                params.put("vnp_Version", "2.1.0");
                params.put("vnp_Command", "pay");

                params.put(
                                "vnp_TmnCode",
                                properties.getTmnCode());

                params.put(
                                "vnp_Amount",
                                amount
                                                .multiply(BigDecimal.valueOf(100))
                                                .toBigIntegerExact()
                                                .toString());

                params.put("vnp_CurrCode", "VND");

                params.put(
                                "vnp_TxnRef",
                                transactionRef);

                params.put(
                                "vnp_OrderInfo",
                                "Thanh toan don hang " + transactionRef);

                params.put("vnp_OrderType", "other");
                params.put("vnp_Locale", "vn");

                params.put(
                                "vnp_ReturnUrl",
                                properties.getReturnUrl());

                params.put(
                                "vnp_IpAddr",
                                normalizeClientIp(clientIp));

                params.put(
                                "vnp_CreateDate",
                                VNP_DATE_FORMAT.format(now));

                params.put(
                                "vnp_ExpireDate",
                                VNP_DATE_FORMAT.format(expiresAt));

                String hashData = buildHashData(params);

                String query = buildQuery(params);

                String secureHash = hmacSha512(
                                properties.getHashSecret(),
                                hashData);

                return properties.getPayUrl()
                                + "?"
                                + query
                                + "&vnp_SecureHash="
                                + secureHash;
        }

        private String normalizeClientIp(
                        String clientIp) {

                if (!StringUtils.hasText(clientIp)
                                || "::1".equals(clientIp)
                                || "0:0:0:0:0:0:0:1".equals(clientIp)) {

                        return "127.0.0.1";
                }

                return clientIp;
        }

        private String buildHashData(
                        Map<String, String> params) {

                StringBuilder hashData = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {

                        if (!StringUtils.hasText(
                                        entry.getValue())) {
                                continue;
                        }

                        if (hashData.length() > 0) {
                                hashData.append('&');
                        }

                        hashData
                                        .append(entry.getKey())
                                        .append('=')
                                        .append(
                                                        urlEncode(
                                                                        entry.getValue()));
                }

                return hashData.toString();
        }

        private String buildQuery(
                        Map<String, String> params) {

                StringBuilder query = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {

                        if (!StringUtils.hasText(
                                        entry.getValue())) {
                                continue;
                        }

                        if (query.length() > 0) {
                                query.append('&');
                        }

                        query
                                        .append(
                                                        urlEncode(
                                                                        entry.getKey()))
                                        .append('=')
                                        .append(
                                                        urlEncode(
                                                                        entry.getValue()));
                }

                return query.toString();
        }

        private String urlEncode(
                        String value) {

                return URLEncoder.encode(
                                value,
                                StandardCharsets.US_ASCII);
        }

        private String hmacSha512(
                        String key,
                        String data) {

                try {

                        Mac hmac512 = Mac.getInstance("HmacSHA512");

                        SecretKeySpec secretKey = new SecretKeySpec(
                                        key.getBytes(
                                                        StandardCharsets.UTF_8),
                                        "HmacSHA512");

                        hmac512.init(secretKey);

                        byte[] bytes = hmac512.doFinal(
                                        data.getBytes(
                                                        StandardCharsets.UTF_8));

                        StringBuilder hash = new StringBuilder();

                        for (byte b : bytes) {

                                hash.append(
                                                String.format(
                                                                "%02x",
                                                                b));
                        }

                        return hash.toString();

                } catch (Exception ex) {

                        throw new IllegalStateException(
                                        "Cannot generate VNPay signature",
                                        ex);
                }
        }

        public boolean verifySignature(
                        Map<String, String> incomingParams) {

                validateConfiguration();

                String receivedHash = incomingParams.get(
                                "vnp_SecureHash");

                if (!StringUtils.hasText(
                                receivedHash)) {

                        return false;
                }

                Map<String, String> signedParams = new TreeMap<>();

                incomingParams.forEach(
                                (key, value) -> {

                                        if (!"vnp_SecureHash"
                                                        .equals(key)
                                                        &&
                                                        !"vnp_SecureHashType"
                                                                        .equals(key)
                                                        &&
                                                        StringUtils.hasText(value)) {

                                                signedParams.put(
                                                                key,
                                                                value);
                                        }
                                });

                String expectedHash = hmacSha512(
                                properties.getHashSecret(),
                                buildHashData(
                                                signedParams));

                return MessageDigest.isEqual(

                                expectedHash
                                                .toLowerCase()
                                                .getBytes(
                                                                StandardCharsets.US_ASCII),

                                receivedHash
                                                .toLowerCase()
                                                .getBytes(
                                                                StandardCharsets.US_ASCII));
        }

        public boolean isExpectedTmnCode(
                        String tmnCode) {

                return StringUtils.hasText(tmnCode)
                                &&
                                properties
                                                .getTmnCode()
                                                .equals(tmnCode);
        }

        private void validateConfiguration() {

                if (!StringUtils.hasText(
                                properties.getTmnCode())
                                ||
                                properties
                                                .getTmnCode()
                                                .startsWith("DEMO_")) {

                        throw new IllegalStateException(
                                        "VNPAY_TMN_CODE is not configured");
                }

                if (!StringUtils.hasText(
                                properties.getHashSecret())
                                ||
                                properties
                                                .getHashSecret()
                                                .startsWith("DEMO_")) {

                        throw new IllegalStateException(
                                        "VNPAY_HASH_SECRET is not configured");
                }
        }
}