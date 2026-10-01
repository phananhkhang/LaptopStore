package org.akira.laptopstore.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.akira.laptopstore.auth.entity.RefreshToken;
import org.akira.laptopstore.auth.repository.RefreshTokenRepository;
import org.akira.laptopstore.auth.service.RefreshTokenService;
import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.security.jwt.JwtProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createRefreshToken(Long userId) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .familyId(UUID.randomUUID())
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional
    public RefreshTokenResult rotateRefreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Refresh token not found"));

        Instant now = Instant.now();

        // Check if token was already revoked
        if (token.getRevokedAt() != null) {
            log.warn("Attempt to use revoked refresh token familyId={}", token.getFamilyId());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "REVOKED_TOKEN", "Refresh token has been revoked");
        }

        // Check if token was already used -> Token Reuse Attack detected!
        if (token.getUsedAt() != null) {
            log.error("Token reuse detected for familyId={}. Revoking entire token family!", token.getFamilyId());
            refreshTokenRepository.revokeFamily(token.getFamilyId(), now);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "TOKEN_REUSE_DETECTED", "Suspicious token activity detected. Session invalidated.");
        }

        // Check if expired
        if (token.getExpiresAt().isBefore(now)) {
            token.setRevokedAt(now);
            refreshTokenRepository.save(token);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Refresh token expired");
        }

        // Mark current token as used
        token.setUsedAt(now);
        refreshTokenRepository.save(token);

        // Issue new token in the same family
        String newRawToken = generateSecureToken();
        String newTokenHash = hashToken(newRawToken);

        RefreshToken newToken = RefreshToken.builder()
                .userId(token.getUserId())
                .tokenHash(newTokenHash)
                .familyId(token.getFamilyId())
                .expiresAt(now.plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();

        refreshTokenRepository.save(newToken);

        return new RefreshTokenResult(token.getUserId(), newRawToken);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
