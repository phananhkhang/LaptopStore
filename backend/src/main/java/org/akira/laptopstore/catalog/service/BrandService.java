package org.akira.laptopstore.catalog.service;

import org.akira.laptopstore.catalog.entity.Brand;
import java.util.List;

public interface BrandService {
    List<Brand> findAllBrands();
    Brand findBrandById(Long id);
    Brand createBrand(Brand brand);
}