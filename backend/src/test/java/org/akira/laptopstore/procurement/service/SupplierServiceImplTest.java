package org.akira.laptopstore.procurement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.exception.ResourceNotFoundException;

import org.akira.laptopstore.procurement.dto.request.CreateSupplierRequest;
import org.akira.laptopstore.procurement.mapper.SupplierMapper;
import org.akira.laptopstore.procurement.repository.SupplierRepository;
import org.akira.laptopstore.procurement.service.impl.SupplierServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceImplTest {
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void createSupplier_NameExists_ShouldThrowBusinessException() {
        // Arrange: Chuẩn bị dữ liệu - Giả lập DB báo tên nhà cung cấp đã tồn tại
        CreateSupplierRequest request = new CreateSupplierRequest("Dell", "0123", "dell@mail.com", "US");
        when(supplierRepository.existsByNameIgnoreCase("Dell")).thenReturn(true);

        // Act & Assert: Thực thi và kiểm tra xem có ném ra lỗi như kỳ vọng không
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            supplierService.createSupplier(request);
        });

        assertEquals("SUPPLIER_NAME_EXISTS", exception.getCode());
        // Đảm bảo tuyệt đối KHÔNG được gọi hàm save() xuống DB
        verify(supplierRepository, never()).save(any()); 
    }

    @Test
    void getSupplierById_NotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(supplierRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.getSupplierById(99L);
        });
    }
}
