package org.akira.laptopstore.payment.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnPayProperties {

    private String payUrl;

    private String tmnCode;

    private String hashSecret;

    private String returnUrl;

    private String ipnUrl;
}