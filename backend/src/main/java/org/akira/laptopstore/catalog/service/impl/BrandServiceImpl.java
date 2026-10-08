package org.akira.laptopstore.catalog.service.impl;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.catalog.dto.request.CreateBrandRequest;
import org.akira.laptopstore.catalog.dto.request.UpdateBrandRequest;
import org.akira.laptopstore.catalog.dto.response.BrandResponse;
import org.akira.laptopstore.catalog.entity.Brand;
import org.akira.laptopstore.catalog.repository.BrandRepository;
import org.akira.laptopstore.catalog.service.BrandService;
import org.akira.laptopstore.catalog.mapper.BrandMapper;

import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandResponse> findAllBrands() {
        // Lấy danh sách từ DB, stream qua Mapper để chuyển thành list DTO siêu ngắn gọn
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toResponse) // Gọi Mapper ở đây
                .toList(); // Hoặc .collect(Collectors.toList())
    }

    @Override
    public BrandResponse findBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        
        // Gọi Mapper để trả về
        return brandMapper.toResponse(brand);
    }
    
    @Override
    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        // 1. Chốt chặn: Kiểm tra tên Brand đã tồn tại chưa
        if (brandRepository.existsByName(request.getName())) {
            throw new BusinessException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "BRAND_NAME_ALREADY_EXISTS", 
                "Tên thương hiệu '" + request.getName() + "' đã tồn tại!"
            );
        }

        // 2. Map dữ liệu và lưu
        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true) // Luôn active khi tạo mới
                .build();

        Brand savedBrand = brandRepository.save(brand);
        
        // 3. Map sang Response DTO (Sử dụng mapper của em)
        return brandMapper.toResponse(savedBrand); 
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, UpdateBrandRequest request) {
        // 1. Tìm Brand cần update
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        // 2. Chốt chặn: Nếu tên bị đổi, kiểm tra xem có trùng với hãng KHÁC không
        if (!existingBrand.getName().equalsIgnoreCase(request.getName())) {
            if (brandRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new BusinessException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, 
                    "BRAND_NAME_ALREADY_EXISTS", 
                    "Tên thương hiệu '" + request.getName() + "' đã bị trùng với hãng khác!"
                );
            }
        }

        // 3. Cập nhật dữ liệu (Dùng Setter, không dùng Builder)
        existingBrand.setName(request.getName());
        existingBrand.setDescription(request.getDescription());

        Brand updatedBrand = brandRepository.save(existingBrand);
        return brandMapper.toResponse(updatedBrand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        // 1. Kiểm tra tồn tại
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        // 2. Xóa!
        // Nhờ phép thuật của @SQLDelete trong Entity, lệnh này sẽ tự động chuyển thành UPDATE active = false!
        brandRepository.delete(existingBrand);
    }
}