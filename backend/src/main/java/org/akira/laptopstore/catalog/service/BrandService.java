package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.dto.request.CreateBrandRequest;
import org.akira.laptopstore.catalog.dto.request.UpdateBrandRequest;
import org.akira.laptopstore.catalog.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {
    
    // 2 hàm cũ của em đã được nâng cấp lên dùng DTO
    List<BrandResponse> findAllBrands();
    BrandResponse findBrandById(Long id);

    // 3 hàm mới cho chuẩn nghiệp vụ Create/Update/Delete
    BrandResponse createBrand(CreateBrandRequest request);
    BrandResponse updateBrand(Long id, UpdateBrandRequest request);
    void deleteBrand(Long id);
}