package org.akira.laptopstore.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.akira.laptopstore.auth.dto.AuthResponse;
import org.akira.laptopstore.auth.dto.LoginRequest;
import org.akira.laptopstore.auth.dto.RefreshTokenResponse;
import org.akira.laptopstore.auth.dto.RegisterRequest;
import org.akira.laptopstore.auth.service.AuthService;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtProperties;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtProperties.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/v1/auth/register - Thành công trả về 201 Created và thiết lập Refresh Cookie")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("strongPassword123");
        request.setFullName("Nguyen Van A");
        request.setPhone("0901234567");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .userId(1L)
                .email("newuser@example.com")
                .fullName("Nguyen Van A")
                .roles(Set.of("ROLE_USER"))
                .build();

        AuthService.AuthResult result = new AuthService.AuthResult(authResponse, "mock-raw-refresh-token");
        when(authService.register(any(RegisterRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(header().exists("Set-Cookie"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Thất bại 400 Bad Request khi email không hợp lệ hoặc mật khẩu rỗng")
    void register_invalidRequest_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email-format");
        request.setPassword("");
        request.setFullName("");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Đăng nhập thành công trả về 200 OK và Set-Cookie refresh token")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("mock-access-token")
                .userId(2L)
                .email("user@example.com")
                .fullName("Tran Van B")
                .roles(Set.of("ROLE_USER"))
                .build();

        AuthService.AuthResult result = new AuthService.AuthResult(authResponse, "new-refresh-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.userId").value(2L))
                .andExpect(header().exists("Set-Cookie"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Thất bại 400 Bad Request khi thiếu mật khẩu")
    void login_missingPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Thành công trả về 200 OK với accessToken mới")
    void refresh_success() throws Exception {
        RefreshTokenResponse refreshResponse = new RefreshTokenResponse("new-jwt-access-token");
        AuthService.RefreshTokenResult result = new AuthService.RefreshTokenResult(refreshResponse, "rotated-refresh-token");
        when(authService.refresh("incoming-refresh-token")).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", "incoming-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-access-token"))
                .andExpect(header().exists("Set-Cookie"));

        verify(authService).refresh("incoming-refresh-token");
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Thành công trả về 204 No Content và xóa Cookie")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("refresh_token", "token-to-invalidate")))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));

        verify(authService).logout("token-to-invalidate");
    }
}
