package org.akira.laptopstore.order.entity.enums;

import org.akira.laptopstore.exception.ValidationException;

import java.util.Locale;

public enum CheckoutPaymentMethod {
    COD,
    VNPAY;

    public static CheckoutPaymentMethod fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return COD;
        }
        String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "COD" -> COD;
            case "VNPAY", "ONLINE" -> VNPAY;
            default -> throw new ValidationException("Unsupported payment method: " + rawValue.trim());
        };
    }
}
