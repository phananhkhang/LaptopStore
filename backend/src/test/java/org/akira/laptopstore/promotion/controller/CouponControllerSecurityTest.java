package org.akira.laptopstore.promotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akira.laptopstore.promotion.dto.request.CreateCouponRequest;
import org.akira.laptopstore.promotion.dto.response.CouponResponse;
import org.akira.laptopstore.promotion.enums.DiscountType;
import org.akira.laptopstore.promotion.service.CouponService;
import org.akira.laptopstore.promotion.service.CouponUsageService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CouponController.class, CouponUsageController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class CouponControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private CouponService couponService;
    @MockitoBean private CouponUsageService couponUsageService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("TDD RED: POST /api/v1/coupons với ROLE_USER bị chặn 403 Forbidden")
    void createCoupon_whenUserRole_shouldReturn403() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");
        CreateCouponRequest request = sampleCreateRequest();

        mockMvc.perform(post("/api/v1/coupons")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(couponService);
    }

    @Test
    @DisplayName("TDD RED: POST /api/v1/coupons với ROLE_ADMIN thành công 201 Created")
    void createCoupon_whenAdminRole_shouldReturn201() throws Exception {
        CustomUserDetails principal = principal(1L, "ROLE_ADMIN");
        CreateCouponRequest request = sampleCreateRequest();
        CouponResponse response = CouponResponse.builder()
                .id(1L)
                .code("DISCOUNT10")
                .discountType(DiscountType.PERCENT)
                .discountValue(new BigDecimal("10"))
                .active(true)
                .build();

        when(couponService.createCoupon(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/coupons")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(couponService).createCoupon(any());
    }

    @Test
    @DisplayName("TDD RED: DELETE /api/v1/coupons/{id} với ROLE_USER bị chặn 403 Forbidden")
    void deleteCoupon_whenUserRole_shouldReturn403() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");

        mockMvc.perform(delete("/api/v1/coupons/1")
                        .with(user(principal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(couponService);
    }

    @Test
    @DisplayName("TDD RED: DELETE /api/v1/coupons/{id} với ROLE_ADMIN thành công 204 No Content")
    void deleteCoupon_whenAdminRole_shouldReturn204() throws Exception {
        CustomUserDetails principal = principal(1L, "ROLE_ADMIN");

        mockMvc.perform(delete("/api/v1/coupons/1")
                        .with(user(principal)))
                .andExpect(status().isNoContent());

        verify(couponService).deleteCoupon(1L);
    }

    private CreateCouponRequest sampleCreateRequest() {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("DISCOUNT10");
        request.setDiscountType(DiscountType.PERCENT);
        request.setDiscountValue(new BigDecimal("10"));
        return request;
    }

    private CustomUserDetails principal(Long id, String role) {
        return new CustomUserDetails(id, "user" + id + "@example.com", "unused", 0,
                "ACTIVE", List.of(new SimpleGrantedAuthority(role)));
    }
}
