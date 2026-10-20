package org.akira.laptopstore.auth.service;

import org.akira.laptopstore.auth.dto.LoginRequest;
import org.akira.laptopstore.auth.dto.RegisterRequest;
import org.akira.laptopstore.auth.service.impl.AuthServiceImpl;
import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.user.dto.UserCredentialDto;
import org.akira.laptopstore.user.service.UserCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserCredentialService userCredentialService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserCredentialDto activeUser;

    @BeforeEach
    void setUp() {
        activeUser = UserCredentialDto.builder()
                .id(1L)
                .email("user@laptopstore.com")
                .passwordHash("$2a$10$hashedPassword")
                .status("ACTIVE")
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    @Test
    @DisplayName("login: Should successfully return accessToken and refreshToken when credentials valid")
    void login_withValidCredentials_shouldSucceed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@laptopstore.com");
        request.setPassword("password123");

        when(userCredentialService.findByEmail("user@laptopstore.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("mock-access-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn("mock-refresh-token");

        AuthService.AuthResult result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.response().getAccessToken()).isEqualTo("mock-access-token");
        assertThat(result.response().getEmail()).isEqualTo("user@laptopstore.com");
        assertThat(result.rawRefreshToken()).isEqualTo("mock-refresh-token");
        verify(jwtTokenProvider).generateAccessToken(any());
        verify(refreshTokenService).createRefreshToken(1L);
    }

    @Test
    @DisplayName("login: Should throw 401 UNAUTHORIZED when user not found")
    void login_whenUserNotFound_shouldThrowUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@laptopstore.com");
        request.setPassword("password123");

        when(userCredentialService.findByEmail("notfound@laptopstore.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(be.getCode()).isEqualTo("INVALID_CREDENTIALS");
                });

        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("login: Should throw 401 UNAUTHORIZED when password does not match")
    void login_whenPasswordMismatch_shouldThrowUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@laptopstore.com");
        request.setPassword("wrongpassword");

        when(userCredentialService.findByEmail("user@laptopstore.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });

        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("login: Should throw 403 FORBIDDEN when account is inactive/suspended")
    void login_whenAccountInactive_shouldThrowForbidden() {
        UserCredentialDto inactiveUser = UserCredentialDto.builder()
                .id(2L)
                .email("banned@laptopstore.com")
                .status("SUSPENDED")
                .build();

        LoginRequest request = new LoginRequest();
        request.setEmail("banned@laptopstore.com");
        request.setPassword("password123");

        when(userCredentialService.findByEmail("banned@laptopstore.com")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(be.getCode()).isEqualTo("ACCOUNT_INACTIVE");
                });
    }

    @Test
    @DisplayName("register: Should encode password, create user and return AuthResult")
    void register_withValidData_shouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@laptopstore.com");
        request.setPassword("rawPassword123");
        request.setFullName("Tran Van B");
        request.setPhone("0912345678");

        when(passwordEncoder.encode("rawPassword123")).thenReturn("$2a$10$newHashedPassword");
        when(userCredentialService.createUser(
                eq("new@laptopstore.com"),
                eq("$2a$10$newHashedPassword"),
                eq("Tran Van B"),
                eq("0912345678"),
                any()
        )).thenReturn(activeUser);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("token-new");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn("refresh-new");

        AuthService.AuthResult result = authService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.response().getAccessToken()).isEqualTo("token-new");
        assertThat(result.rawRefreshToken()).isEqualTo("refresh-new");
        verify(passwordEncoder).encode("rawPassword123");
        verify(userCredentialService).createUser(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("refresh: Should throw UNAUTHORIZED when refresh token is null or blank")
    void refresh_withBlankToken_shouldThrowUnauthorized() {
        assertThatThrownBy(() -> authService.refresh(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> authService.refresh("   "))
                .isInstanceOf(BusinessException.class);
    }
}
