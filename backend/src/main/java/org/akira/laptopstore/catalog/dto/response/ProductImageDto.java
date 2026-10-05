package org.akira.laptopstore.catalog.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageDto {
    private Long id;
    private String imageUrl;
    private Boolean isPrimary;
}