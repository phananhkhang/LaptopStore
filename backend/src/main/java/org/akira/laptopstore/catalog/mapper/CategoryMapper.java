package org.akira.laptopstore.catalog.mapper;

import org.akira.laptopstore.catalog.dto.response.CategoryResponse;
import org.akira.laptopstore.catalog.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // MapStruct sẽ tự động viết thân hàm chuyển từ Entity Category sang DTO CategoryResponse
    CategoryResponse toResponse(Category category);

}
