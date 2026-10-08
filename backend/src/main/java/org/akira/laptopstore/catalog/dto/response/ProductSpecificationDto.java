package org.akira.laptopstore.catalog.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSpecificationDto {
    private String cpu, gpu, screenSize, screenResolution, refreshRate, battery, weight, os;
}
