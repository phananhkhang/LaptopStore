package org.akira.laptopstore.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> OrderPageResponse<T> from(Page<T> page) {
        return new OrderPageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }
}
