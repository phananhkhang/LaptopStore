package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.entity.Category;
import org.akira.laptopstore.catalog.repository.CategoryRepository;
import org.akira.laptopstore.catalog.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    // Anh dùng @RequiredArgsConstructor của Lombok kết hợp với từ khóa 'final'
    // để Spring tự động Inject (tiêm) CategoryRepository vào đây mà không cần viết hàm Constructor dài dòng.
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category findCategoryById(Long id) {
        // Tạm thời ném ra RuntimeException cơ bản, tuần sau team mình sẽ làm Global Exception sau.
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
}