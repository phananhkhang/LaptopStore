package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.request.CreateCategoryRequest;
import org.akira.laptopstore.catalog.dto.request.UpdateCategoryRequest;
import org.akira.laptopstore.catalog.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAllCategories();
    CategoryResponse findCategoryById(Long id);

    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);
    void deleteCategory(Long id);
}