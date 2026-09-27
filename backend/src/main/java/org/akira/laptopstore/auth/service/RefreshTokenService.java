package org.akira.laptopstore.auth.service;

public interface RefreshTokenService {
    String createRefreshToken(Long userId);
    RefreshTokenResult rotateRefreshToken(String rawRefreshToken);
    void revokeRefreshToken(String rawRefreshToken);

    record RefreshTokenResult(Long userId, String newRawRefreshToken) {}
}
