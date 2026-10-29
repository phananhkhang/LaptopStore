package org.akira.laptopstore.config; // Điều chỉnh package nếu cần

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.akira.laptopstore.catalog.entity.Brand;
import org.akira.laptopstore.catalog.entity.Product;
import org.akira.laptopstore.catalog.entity.Category;
import org.akira.laptopstore.catalog.entity.ProductVariant;

import org.akira.laptopstore.catalog.repository.CategoryRepository;
import org.akira.laptopstore.catalog.repository.BrandRepository;
import org.akira.laptopstore.catalog.repository.ProductRepository;
import org.akira.laptopstore.catalog.repository.ProductVariantRepository;



import org.akira.laptopstore.procurement.repository.SupplierRepository;
import org.akira.laptopstore.procurement.entity.Supplier;


import org.akira.laptopstore.user.entity.Role;
import org.akira.laptopstore.user.entity.User;

import org.akira.laptopstore.user.repository.RoleRepository;
import org.akira.laptopstore.user.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Bổ sung kho lưu trữ Role
    private final PasswordEncoder passwordEncoder;
    private final SupplierRepository supplierRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "kietadmin@gmail.com";

        if (userRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            // 1. Tìm Role ADMIN trong DB, nếu chưa có thì tự động tạo mới
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            // 2. Khởi tạo tài khoản với Set<Role> và trường passwordHash chuẩn xác
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("password123")) // Đã đổi thành passwordHash
                    .fullName("Huỳnh Thái Kiệt")
                    .phone("0123456789")
                    .status("ACTIVE")
                    .roles(Set.of(adminRole)) // Truyền chính xác Set<Role>
                    .build();

            userRepository.save(admin);
            log.info("Da khoi tao thanh cong tai khoan ADMIN mac dinh: {}", adminEmail);
        }
        // 1. Khởi tạo Nhà cung cấp (Supplier)
        if (supplierRepository.count() == 0) {
            supplierRepository.save(Supplier.builder()
                    .name("Nhà phân phối Asus VN")
                    .phone("0909123456")
                    .email("contact@asus.vn")
                    .address("Quận 1, TP.HCM")
                    .status("ACTIVE")
                    .build());
            log.info("Da khoi tao Supplier mac dinh.");
        }

        // 2. Khởi tạo Brand và Category
        if (categoryRepository.count() == 0 && brandRepository.count() == 0) {
            Category category = categoryRepository.save(Category.builder().name("Laptop Gaming").active(true).build());
            Brand brand = brandRepository.save(Brand.builder().name("Asus").active(true).build());

            // 3. Khởi tạo Product và ProductVariant (Tồn kho ban đầu = 0)
            Product product = Product.builder()
                    .name("Asus ROG Strix G15")
                    .description("Laptop Gaming quoc dan")
                    .status("ACTIVE")
                    .active(true)
                    .category(category)
                    .brand(brand)
                    .build();

            product = productRepository.save(product);

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .ram("16GB")
                    .storage("512GB SSD")
                    .color("Eclipse Gray")
                    .price(new java.math.BigDecimal("25000000"))
                    .quantity(0) // TỒN KHO BAN ĐẦU BẰNG 0
                    .sku("ASUS-ROG-G15-01")
                    .status("ACTIVE")
                    .active(true)
                    .build();

            productVariantRepository.save(variant);
            log.info("Da khoi tao Product và Variant mac dinh.");
        }

        else {
            log.info("Tai khoan ADMIN da ton tai, bo qua buoc khoi tao.");
        }
    }
}