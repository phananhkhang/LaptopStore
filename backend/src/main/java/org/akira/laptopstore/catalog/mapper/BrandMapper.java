package org.akira.laptopstore.catalog.mapper;

import org.akira.laptopstore.catalog.dto.response.BrandResponse;
import org.akira.laptopstore.catalog.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    // MapStruct sẽ tự động viết thân hàm chuyển từ Entity Brand sang DTO BrandResponse
    BrandResponse toResponse(Brand brand);
}
