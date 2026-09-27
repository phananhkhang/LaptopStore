package org.akira.laptopstore.auth.service;

import org.akira.laptopstore.auth.dto.AuthResponse;
import org.akira.laptopstore.auth.dto.LoginRequest;
import org.akira.laptopstore.auth.dto.RefreshTokenResponse;
import org.akira.laptopstore.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResult register(RegisterRequest request);
    AuthResult login(LoginRequest request);
    RefreshTokenResult refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);

    record AuthResult(AuthResponse response, String rawRefreshToken) {}
    record RefreshTokenResult(RefreshTokenResponse response, String newRawRefreshToken) {}
}
