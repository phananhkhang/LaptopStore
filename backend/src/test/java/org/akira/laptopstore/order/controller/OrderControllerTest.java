package org.akira.laptopstore.order.controller;

import org.akira.laptopstore.order.dto.request.CheckoutPreviewRequest;
import org.akira.laptopstore.order.dto.request.CreateOrderRequest;
import org.akira.laptopstore.order.dto.response.CheckoutPreviewResponse;
import org.akira.laptopstore.order.dto.response.OrderResponse;
import org.akira.laptopstore.order.dto.response.OrderSummaryResponse;
import org.akira.laptopstore.order.entity.enums.OrderStatus;
import org.akira.laptopstore.order.service.OrderService;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private OrderService orderService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    @Test
    void userSearchAlwaysScopesToPrincipal() throws Exception {
        when(orderService.searchOrders(eq(42L), eq(OrderStatus.PACKING), eq("LS"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/me/orders?search=LS&status=PACKING").with(user(principal(42L, "ROLE_USER"))))
                .andExpect(status().isOk());
        verify(orderService).searchOrders(eq(42L), eq(OrderStatus.PACKING), eq("LS"), any());
    }

    @Test
    void staffDetailEndpointRejectsRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/staff/orders/10").with(user(principal(42L, "ROLE_USER"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orderService);
    }

    @Test
    void staffCanReadOrderDetail() throws Exception {
        mockMvc.perform(get("/api/v1/staff/orders/10").with(user(principal(99L, "ROLE_STAFF"))))
                .andExpect(status().isOk());
        verify(orderService).getStaffOrder(10L);
    }

    @Test
    void userCanReadOnlyTheirOrderEndpoints() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");
        OrderSummaryResponse summary = new OrderSummaryResponse(10L, "ORD-10", OrderStatus.CONFIRMED,
                2, new BigDecimal("25000000"), "Nguyen Van A", Instant.parse("2026-10-01T10:00:00Z"));
        OrderResponse detail = new OrderResponse(10L, 42L, "ORD-10", OrderStatus.CONFIRMED,
                new BigDecimal("25000000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("25000000"),
                "Nguyen Van A", "0900000000", "Ho Chi Minh City", null,
                List.of(), List.of(), Instant.parse("2026-10-01T10:00:00Z"), Instant.parse("2026-10-01T10:00:00Z"));
        when(orderService.getOwnOrders(eq(42L), any())).thenReturn(new PageImpl<>(List.of(summary)));
        when(orderService.getOwnOrder(42L, 10L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/me/orders").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderCode").value("ORD-10"));
        mockMvc.perform(get("/api/v1/me/orders/10").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42L));

        verify(orderService).getOwnOrders(eq(42L), any());
        verify(orderService).getOwnOrder(42L, 10L);
    }

    @Test
    void userCanPreviewAndCreateOrderFromCheckout() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");
        CheckoutPreviewResponse preview = CheckoutPreviewResponse.builder()
                .addressId(15L)
                .recipientName("Nguyen Van A")
                .recipientPhone("0900000000")
                .shippingAddress("123 Nguyen Trai, Q5, TP.HCM")
                .paymentMethod("COD")
                .couponCode("STUDENT10")
                .subtotal(new BigDecimal("25000000"))
                .discountAmount(new BigDecimal("500000"))
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("24500000"))
                .totalItems(1)
                .items(List.of())
                .build();
        OrderResponse created = new OrderResponse(125L, 42L, "ORD-125", OrderStatus.WAITING_CONFIRMATION,
                new BigDecimal("25000000"), new BigDecimal("500000"), BigDecimal.ZERO, new BigDecimal("24500000"),
                "Nguyen Van A", "0900000000", "123 Nguyen Trai, Q5, TP.HCM", "STUDENT10",
                List.of(), List.of(), Instant.parse("2026-10-14T10:00:00Z"), Instant.parse("2026-10-14T10:00:00Z"));

        when(orderService.previewCheckout(eq(42L), any(CheckoutPreviewRequest.class))).thenReturn(preview);
        when(orderService.createOrder(eq(42L), any(CreateOrderRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/me/orders/preview")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":15,\"paymentMethod\":\"COD\",\"couponCode\":\"STUDENT10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(25000000))
                .andExpect(jsonPath("$.discountAmount").value(500000))
                .andExpect(jsonPath("$.totalAmount").value(24500000));

        mockMvc.perform(post("/api/v1/me/orders")
                        .with(user(principal))
                        .header("Idempotency-Key", "checkout-key-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":15,\"paymentMethod\":\"COD\",\"couponCode\":\"STUDENT10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(125L))
                .andExpect(jsonPath("$.orderCode").value("ORD-125"))
                .andExpect(jsonPath("$.discountAmount").value(500000))
                .andExpect(jsonPath("$.totalAmount").value(24500000))
                .andExpect(jsonPath("$.couponCodeSnapshot").value("STUDENT10"));

        verify(orderService).previewCheckout(eq(42L), any(CheckoutPreviewRequest.class));
        verify(orderService).createOrder(eq(42L), any(CreateOrderRequest.class));
    }

    @Test
    void staffListRequiresStaffOrAdmin() throws Exception {
        when(orderService.getAllOrders(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/staff/orders").with(user(principal(42L, "ROLE_USER"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/staff/orders").with(user(principal(84L, "ROLE_STAFF"))))
                .andExpect(status().isOk());

        verify(orderService).getAllOrders(any());
    }

    @Test
    void myOrderEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    private CustomUserDetails principal(Long id, String role) {
        return new CustomUserDetails(id, "user" + id + "@example.com", "unused", 0,
                "ACTIVE", List.of(new SimpleGrantedAuthority(role)));
    }
}
