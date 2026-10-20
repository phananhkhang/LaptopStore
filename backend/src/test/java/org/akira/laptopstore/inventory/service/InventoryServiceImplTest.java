package org.akira.laptopstore.inventory.service;

import org.akira.laptopstore.catalog.dto.response.StockChangeResult;

import org.akira.laptopstore.catalog.service.CatalogCheckoutService;
import org.akira.laptopstore.catalog.service.CatalogVariantQueryService;


import org.akira.laptopstore.exception.ValidationException;


import org.akira.laptopstore.inventory.entity.StockMovement;
import org.akira.laptopstore.inventory.repository.StockMovementRepository;
import org.akira.laptopstore.inventory.service.impl.InventoryServiceImpl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {
@Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CatalogCheckoutService catalogCheckoutService;

    @Mock
    private CatalogVariantQueryService catalogVariantQueryService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void adjustStock_ValidRequest_ShouldSaveMovement() {
        // Arrange (Chuẩn bị dữ liệu)
        Long variantId = 1L;
        int delta = 10;
        String reason = "Nhập bù hàng thất lạc";
        Long actorId = 99L;

        StockChangeResult mockResult = new StockChangeResult(variantId, 50, 60);
        when(catalogCheckoutService.adjustStock(variantId, delta)).thenReturn(mockResult);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act (Thực thi)
        inventoryService.adjustStock(variantId, delta, reason, actorId);

        // Assert (Kiểm chứng)
        verify(catalogCheckoutService, times(1)).adjustStock(variantId, delta);
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void adjustStock_DeltaZero_ShouldThrowException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            inventoryService.adjustStock(1L, 0, "Lý do hợp lệ", 99L);
        });
        assertEquals("Delta điều chỉnh kho phải khác 0", exception.getMessage());
    }

    @Test
    void adjustStock_EmptyReason_ShouldThrowException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            inventoryService.adjustStock(1L, 5, "   ", 99L);
        });
        assertEquals("Bắt buộc phải nhập lý do khi điều chỉnh kho thủ công", exception.getMessage());
    }
}