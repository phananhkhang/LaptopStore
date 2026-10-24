package org.akira.laptopstore.procurement.controller;

import org.akira.laptopstore.procurement.dto.request.CreatePurchaseOrderRequest;
import org.akira.laptopstore.procurement.dto.response.PurchaseOrderResponse;
import org.akira.laptopstore.procurement.service.PurchaseOrderService;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderControllerTest {

    @Mock 
    private PurchaseOrderService purchaseOrderService;
    
    @InjectMocks 
    private PurchaseOrderController purchaseOrderController;

    @Test
    void createPurchaseOrder_ShouldReturn201() {
        CustomUserDetails userDetails = new CustomUserDetails(99L, "staff@mail.com", "pass", 1, "ACTIVE", List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(1L, "Nhập hàng", List.of());
        PurchaseOrderResponse mockResponse = PurchaseOrderResponse.builder().id(1L).status("DRAFT").build();
        
        when(purchaseOrderService.createPurchaseOrder(eq(99L), any())).thenReturn(mockResponse);

        ResponseEntity<PurchaseOrderResponse> response = purchaseOrderController.createPurchaseOrder(request, userDetails);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("DRAFT", response.getBody().getStatus());
    }
}