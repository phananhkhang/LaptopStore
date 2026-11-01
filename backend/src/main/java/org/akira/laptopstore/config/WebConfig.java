package org.akira.laptopstore.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    private final String uploadDir;

    public WebConfig(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ mọi request bắt đầu bằng /uploads/ vào thư mục ảnh vật lý.
        Path location = resolveUploadDir();
        // Location thư mục phải kết thúc bằng "/" (Path.toUri chỉ thêm khi thư mục đã tồn tại).
        String locationUri = location.toUri().toString();
        if (!locationUri.endsWith("/")) {
            locationUri += "/";
        }
        log.info("Serving /uploads/** from {}", locationUri);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(locationUri);
    }

    /**
     * Đường dẫn tương đối phụ thuộc thư mục chạy JVM. Khi chạy từ gốc repo (IDE) thay vì
     * thư mục backend/, "uploads" không tồn tại nên thử thêm "backend/uploads".
     */
    private Path resolveUploadDir() {
        Path configured = Path.of(uploadDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(configured) && !Path.of(uploadDir).isAbsolute()) {
            Path fromRepoRoot = Path.of("backend").resolve(uploadDir).toAbsolutePath().normalize();
            if (Files.isDirectory(fromRepoRoot)) {
                return fromRepoRoot;
            }
        }
        return configured;
    }
}
