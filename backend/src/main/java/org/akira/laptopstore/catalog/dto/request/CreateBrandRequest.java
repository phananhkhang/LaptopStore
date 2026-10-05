package org.akira.laptopstore.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBrandRequest {
    @NotBlank(message = "Tên thương hiệu không được để rỗng")
    private String name;
    
    private String description;
}