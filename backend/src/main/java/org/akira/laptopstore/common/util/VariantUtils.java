package org.akira.laptopstore.common.util;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VariantUtils {

    private static final String DEFAULT_DELIMITER = " - ";

    private VariantUtils() {}

    /**
     * Ghép các thuộc tính cấu hình thành tên biến thể hiển thị (variantName).
     * <p>
     * Ví dụ:
     * formatVariantName("16GB", "512GB SSD", "Xám không gian")
     * -> "16GB - 512GB SSD - Xám không gian"
     * <p>
     * Tự động bỏ qua các trường null hoặc rỗng, tránh lỗi định dạng nếu thiếu thuộc tính.
     *
     * @param ram     Dung lượng RAM (ví dụ: "16GB")
     * @param storage Dung lượng ổ cứng (ví dụ: "512GB")
     * @param color   Màu sắc (ví dụ: "Xám không gian")
     * @return Chuỗi tên biến thể chuẩn hóa
     */
    public static String formatVariantName(String ram, String storage, String color) {
        return Stream.of(ram, storage, color)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(DEFAULT_DELIMITER));
    }

    /**
     * Ghép tên sản phẩm kèm thông số biến thể đầy đủ để hiển thị hóa đơn / tiêu đề.
     * <p>
     * Ví dụ:
     * formatFullProductName("MacBook Air M3", "16GB", "512GB", "Xám")
     * -> "MacBook Air M3 (16GB - 512GB - Xám)"
     *
     * @param productName Tên sản phẩm chính
     * @param ram         Dung lượng RAM
     * @param storage     Dung lượng ổ cứng
     * @param color       Màu sắc
     * @return Tên đầy đủ gồm tên sản phẩm và cấu hình
     */
    public static String formatFullProductName(String productName, String ram, String storage, String color) {
        String variantName = formatVariantName(ram, storage, color);
        if (productName == null || productName.isBlank()) {
            return variantName;
        }
        if (variantName.isEmpty()) {
            return productName.trim();
        }
        return productName.trim() + " (" + variantName + ")";
    }
}
