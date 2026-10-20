package org.akira.laptopstore.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akira.laptopstore.review.dto.request.CreateReviewRequest;
import org.akira.laptopstore.review.dto.request.UpdateReviewVisibilityRequest;
import org.akira.laptopstore.review.dto.response.ReviewResponse;
import org.akira.laptopstore.review.service.ReviewService;
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

@WebMvcTest({ReviewController.class, StaffReviewController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class ReviewControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private ReviewService reviewService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("TDD RED: POST /api/v1/reviews yêu cầu đăng nhập, ẩn danh trả về 401")
    void createReview_whenAnonymous_shouldReturn401() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(101L);
        request.setRating(5);
        request.setComment("Great product!");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
    }

    @Test
    @DisplayName("TDD RED: POST /api/v1/reviews trích xuất đúng userId từ token JWT, không hardcode 1L")
    void createReview_withUserToken_shouldExtractUserIdFromJwt() throws Exception {
        CustomUserDetails principal = principal(99L, "ROLE_USER");
        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderItemId(101L);
        request.setRating(5);
        request.setComment("Great product!");

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .orderItemId(101L)
                .rating(5)
                .comment("Great product!")
                .visible(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(reviewService.create(eq(99L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(reviewService).create(eq(99L), any());
    }

    @Test
    @DisplayName("TDD RED: PUT /api/v1/staff/reviews/{id}/visibility với ROLE_USER bị chặn 403 Forbidden")
    void updateVisibility_whenUserRole_shouldReturn403() throws Exception {
        CustomUserDetails principal = principal(42L, "ROLE_USER");
        UpdateReviewVisibilityRequest request = new UpdateReviewVisibilityRequest();
        request.setVisible(false);

        mockMvc.perform(put("/api/v1/staff/reviews/1/visibility")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reviewService);
    }

    @Test
    @DisplayName("TDD RED: PUT /api/v1/staff/reviews/{id}/visibility với ROLE_STAFF thành công 200 OK")
    void updateVisibility_whenStaffRole_shouldReturn200() throws Exception {
        CustomUserDetails principal = principal(84L, "ROLE_STAFF");
        UpdateReviewVisibilityRequest request = new UpdateReviewVisibilityRequest();
        request.setVisible(false);

        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .orderItemId(101L)
                .rating(5)
                .comment("Great product!")
                .visible(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(reviewService.setVisibility(1L, false)).thenReturn(response);

        mockMvc.perform(put("/api/v1/staff/reviews/1/visibility")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(reviewService).setVisibility(1L, false);
    }

    private CustomUserDetails principal(Long id, String role) {
        return new CustomUserDetails(id, "user" + id + "@example.com", "unused", 0,
                "ACTIVE", List.of(new SimpleGrantedAuthority(role)));
    }
}
