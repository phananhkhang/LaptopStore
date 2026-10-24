package org.akira.laptopstore.procurement.controller;

import org.akira.laptopstore.procurement.dto.request.CreateSupplierRequest;
import org.akira.laptopstore.procurement.dto.response.SupplierResponse;
import org.akira.laptopstore.procurement.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierControllerTest {

    @Mock 
    private SupplierService supplierService;
    
    @InjectMocks 
    private SupplierController supplierController;

    @Test
    void getAllSuppliers_ShouldReturn200() {
        ResponseEntity<?> response = supplierController.getAllSuppliers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createSupplier_ShouldReturn201() {
        CreateSupplierRequest request = new CreateSupplierRequest("Asus", "0123", "asus@mail.com", "TW");
        SupplierResponse mockResponse = SupplierResponse.builder().id(1L).name("Asus").build();
        
        when(supplierService.createSupplier(any())).thenReturn(mockResponse);

        ResponseEntity<SupplierResponse> response = supplierController.createSupplier(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Asus", response.getBody().getName());
    }
}