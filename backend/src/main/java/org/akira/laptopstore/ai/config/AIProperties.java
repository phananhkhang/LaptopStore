package org.akira.laptopstore.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Getter
@Setter
public class AIProperties {
    private boolean enabled = true;
    private String provider = "gemini";
    private String apiKey = "mock-key";
    private String model = "gemini-1.5-flash";
    private long timeoutMs = 10000;
}
