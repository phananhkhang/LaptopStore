package org.akira.laptopstore.cart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class AddCartItemRequest {
    private Long productVariantId;
    private Integer quantity;
}
