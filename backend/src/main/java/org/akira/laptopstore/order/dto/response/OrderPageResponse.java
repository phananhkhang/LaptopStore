package org.akira.laptopstore.order.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record OrderPageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
    public static <T> OrderPageResponse<T> from(Page<T> page) {
        return new OrderPageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }
}
