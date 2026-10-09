package org.akira.laptopstore.order.util;

import java.util.Locale;
import java.util.UUID;

/** Generates unique, human-readable order codes. */
public final class OrderCodeGenerator {
    private OrderCodeGenerator() {
    }

    public static String generate() {
        return "ORD-" + UUID.randomUUID().toString()
                .replace("-", "")
                .toUpperCase(Locale.ROOT);
    }
}
