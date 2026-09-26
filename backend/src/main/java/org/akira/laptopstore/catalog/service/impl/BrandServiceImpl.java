package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.catalog.entity.Brand;
import org.akira.laptopstore.catalog.repository.BrandRepository;
import org.akira.laptopstore.catalog.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<Brand> findAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public Brand findBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
    }

    @Override
    public Brand createBrand(Brand brand) {
        return brandRepository.save(brand);
    }
}