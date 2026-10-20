package org.akira.laptopstore.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.akira.laptopstore.user.dto.ChangeUserStatusRequest;
import org.akira.laptopstore.user.dto.UserProfileResponse;
import org.akira.laptopstore.user.service.UserService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails createPrincipal(Long id, String role) {
        return new CustomUserDetails(
                id,
                "admin@example.com",
                "secret",
                0,
                "ACTIVE",
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - Chưa đăng nhập trả về 401 Unauthorized")
    void getAllUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - Tài khoản ROLE_USER truy cập trả về 403 Forbidden")
    void getAllUsers_asUser_returns403() throws Exception {
        CustomUserDetails userPrincipal = createPrincipal(10L, "ROLE_USER");

        mockMvc.perform(get("/api/v1/admin/users").with(user(userPrincipal)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - Tài khoản ROLE_ADMIN truy cập thành công trả về 200 OK")
    void getAllUsers_asAdmin_returns200() throws Exception {
        CustomUserDetails adminPrincipal = createPrincipal(1L, "ROLE_ADMIN");
        UserProfileResponse userResponse = UserProfileResponse.builder()
                .id(2L)
                .email("customer@example.com")
                .fullName("Customer Name")
                .status("ACTIVE")
                .roles(Set.of("ROLE_USER"))
                .createdAt(Instant.now())
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/v1/admin/users").with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("customer@example.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/users/{id}/status - Admin cập nhật trạng thái người dùng trả về 200 OK")
    void updateUserStatus_asAdmin_returns200() throws Exception {
        CustomUserDetails adminPrincipal = createPrincipal(1L, "ROLE_ADMIN");
        ChangeUserStatusRequest request = new ChangeUserStatusRequest();
        request.setStatus("SUSPENDED");

        UserProfileResponse updatedUser = UserProfileResponse.builder()
                .id(2L)
                .email("customer@example.com")
                .fullName("Customer Name")
                .status("SUSPENDED")
                .roles(Set.of("ROLE_USER"))
                .createdAt(Instant.now())
                .build();

        when(userService.updateUserStatus(eq(2L), any(ChangeUserStatusRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/v1/admin/users/2/status")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        verify(userService).updateUserStatus(eq(2L), any(ChangeUserStatusRequest.class));
    }
}
