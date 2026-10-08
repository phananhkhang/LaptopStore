package org.akira.laptopstore.catalog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name, description, status, brandName, categoryName;
    private ProductSpecificationDto specification;
    private List<ProductImageDto> images;
    private List<VariantSnapshot> variants;
}