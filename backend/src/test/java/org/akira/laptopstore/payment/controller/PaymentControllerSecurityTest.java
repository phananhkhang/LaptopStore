package org.akira.laptopstore.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akira.laptopstore.payment.dto.request.RecordCodPaymentRequest;
import org.akira.laptopstore.payment.dto.request.StartPaymentRequest;
import org.akira.laptopstore.payment.dto.response.PaymentResponse;
import org.akira.laptopstore.payment.enums.PaymentMethod;
import org.akira.laptopstore.payment.enums.PaymentStatus;
import org.akira.laptopstore.payment.service.PaymentService;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class PaymentControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private PaymentService paymentService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("TDD RED: POST /api/v1/payments yêu cầu đăng nhập, ẩn danh trả về 401")
    void startPayment_whenAnonymous_shouldReturn401() throws Exception {
        StartPaymentRequest request = new StartPaymentRequest();
        request.setOrderId(10L);
        request.setMethod(PaymentMethod.VNPAY);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }

    @Test
    @DisplayName("TDD RED: POST /api/v1/payments với user đã đăng nhập thành công 201 Created")
    void startPayment_whenAuthenticatedUser_shouldReturn201() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");
        StartPaymentRequest request = new StartPaymentRequest();
        request.setOrderId(10L);
        request.setMethod(PaymentMethod.VNPAY);

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .orderId(10L)
                .method(PaymentMethod.VNPAY)
                .amount(new BigDecimal("15000000"))
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(paymentService.startPayment(eq(42L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(paymentService).startPayment(eq(42L), any());
    }

    @Test
    @DisplayName("TDD RED: PUT /api/v1/payments/cod với ROLE_USER bị chặn 403 Forbidden")
    void recordCod_whenUserRole_shouldReturn403() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");
        RecordCodPaymentRequest request = new RecordCodPaymentRequest();
        request.setOrderId(10L);

        mockMvc.perform(put("/api/v1/payments/cod")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentService);
    }

    @Test
    @DisplayName("TDD RED: PUT /api/v1/payments/cod với ROLE_STAFF thành công 200 OK")
    void recordCod_whenStaffRole_shouldReturn200() throws Exception {
        CustomUserDetails principal = principal(84L, "ROLE_STAFF");
        RecordCodPaymentRequest request = new RecordCodPaymentRequest();
        request.setOrderId(10L);

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .orderId(10L)
                .method(PaymentMethod.COD)
                .amount(new BigDecimal("15000000"))
                .status(PaymentStatus.PAID)
                .createdAt(Instant.now())
                .build();

        when(paymentService.recordCodPayment(any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/payments/cod")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(paymentService).recordCodPayment(any());
    }

    private CustomUserDetails principal(Long id, String role) {
        return new CustomUserDetails(id, "user" + id + "@example.com", "unused", 0,
                "ACTIVE", List.of(new SimpleGrantedAuthority(role)));
    }
}
