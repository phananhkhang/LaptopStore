package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAllCategories();
    Category findCategoryById(Long id);
    Category createCategory(Category category);
}