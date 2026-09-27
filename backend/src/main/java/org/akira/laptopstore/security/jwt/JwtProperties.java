package org.akira.laptopstore.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret = "9a4f2c8d3b7a1e6f4c8b2d0e9f1a7b3c5d8e2f4a6b0c1d3e5f7a9b2c4d6e8f0a";
    private long accessTokenExpirationMs = 900000;
    private long refreshTokenExpirationMs = 604800000;
    private String refreshCookieName = "refresh_token";
}
