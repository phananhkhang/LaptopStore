package org.akira.laptopstore.procurement.service.impl;

import lombok.RequiredArgsConstructor;

import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import org.akira.laptopstore.procurement.dto.request.CreateSupplierRequest;
import org.akira.laptopstore.procurement.dto.request.UpdateSupplierRequest;
import org.akira.laptopstore.procurement.dto.response.SupplierResponse;

import org.akira.laptopstore.procurement.entity.Supplier;
import org.akira.laptopstore.procurement.mapper.SupplierMapper;
import org.akira.laptopstore.procurement.repository.SupplierRepository;
import org.akira.laptopstore.procurement.service.SupplierService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAllByOrderByIdDesc()
                .stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        if (supplierRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SUPPLIER_NAME_EXISTS", 
                    "Tên nhà cung cấp '" + request.getName() + "' đã tồn tại!");
        }

        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .status("ACTIVE")
                .build();

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));

        if (!supplier.getName().equalsIgnoreCase(request.getName())) {
            if (supplierRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "SUPPLIER_NAME_EXISTS", 
                        "Tên nhà cung cấp '" + request.getName() + "' đã bị trùng với nhà cung cấp khác!");
            }
        }

        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setStatus(request.getStatus());

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public void toggleSupplierStatus(Long id, String status) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + id));
        
        supplier.setStatus(status);
        supplierRepository.save(supplier);
    }
    
}