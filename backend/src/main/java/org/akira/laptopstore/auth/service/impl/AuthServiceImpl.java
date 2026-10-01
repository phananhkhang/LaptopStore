package org.akira.laptopstore.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akira.laptopstore.auth.dto.AuthResponse;
import org.akira.laptopstore.auth.dto.LoginRequest;
import org.akira.laptopstore.auth.dto.RefreshTokenResponse;
import org.akira.laptopstore.auth.dto.RegisterRequest;
import org.akira.laptopstore.auth.service.AuthService;
import org.akira.laptopstore.auth.service.RefreshTokenService;
import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.user.dto.UserCredentialDto;
import org.akira.laptopstore.user.service.UserCredentialService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialService userCredentialService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResult register(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserCredentialDto credential = userCredentialService.createUser(
                request.getEmail(),
                encodedPassword,
                request.getFullName(),
                request.getPhone(),
                Set.of("ROLE_USER")
        );

        CustomUserDetails userDetails = buildUserDetails(credential);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String rawRefreshToken = refreshTokenService.createRefreshToken(credential.getId());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .userId(credential.getId())
                .email(credential.getEmail())
                .fullName(request.getFullName())
                .roles(credential.getRoles())
                .build();

        return new AuthResult(authResponse, rawRefreshToken);
    }

    @Override
    @Transactional
    public AuthResult login(LoginRequest request) {
        UserCredentialDto credential = userCredentialService.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password"));

        if (!"ACTIVE".equalsIgnoreCase(credential.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", "Account is not active or has been suspended");
        }

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
        }

        CustomUserDetails userDetails = buildUserDetails(credential);
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String rawRefreshToken = refreshTokenService.createRefreshToken(credential.getId());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .userId(credential.getId())
                .email(credential.getEmail())
                .roles(credential.getRoles())
                .build();

        return new AuthResult(authResponse, rawRefreshToken);
    }

    @Override
    @Transactional
    public RefreshTokenResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "MISSING_REFRESH_TOKEN", "Refresh token cookie is required");
        }

        RefreshTokenService.RefreshTokenResult rotationResult = refreshTokenService.rotateRefreshToken(rawRefreshToken);

        UserCredentialDto credential = userCredentialService.findById(rotationResult.userId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found"));

        if (!"ACTIVE".equalsIgnoreCase(credential.getStatus())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", "Account is not active");
        }

        CustomUserDetails userDetails = buildUserDetails(credential);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        return new RefreshTokenResult(
                RefreshTokenResponse.builder().accessToken(newAccessToken).build(),
                rotationResult.newRawRefreshToken()
        );
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(rawRefreshToken);
        }
    }

    private CustomUserDetails buildUserDetails(UserCredentialDto credential) {
        List<SimpleGrantedAuthority> authorities = credential.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new CustomUserDetails(
                credential.getId(),
                credential.getEmail(),
                credential.getPasswordHash(),
                credential.getTokenVersion(),
                credential.getStatus(),
                authorities
        );
    }
}
