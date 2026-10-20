package org.akira.laptopstore.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.akira.laptopstore.security.CustomAccessDeniedHandler;
import org.akira.laptopstore.security.JwtAuthenticationEntryPoint;
import org.akira.laptopstore.security.SecurityConfig;
import org.akira.laptopstore.security.filter.JwtAuthenticationFilter;
import org.akira.laptopstore.security.jwt.JwtTokenProvider;
import org.akira.laptopstore.security.principal.CustomUserDetails;
import org.akira.laptopstore.security.principal.CustomUserDetailsService;
import org.akira.laptopstore.user.dto.AddressResponse;
import org.akira.laptopstore.user.dto.CreateAddressRequest;
import org.akira.laptopstore.user.dto.UpdateProfileRequest;
import org.akira.laptopstore.user.dto.UserProfileResponse;
import org.akira.laptopstore.user.service.AddressService;
import org.akira.laptopstore.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private CustomUserDetails createUserPrincipal(Long id) {
        return new CustomUserDetails(
                id,
                "user" + id + "@example.com",
                "hashedpassword",
                0,
                "ACTIVE",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("GET /api/v1/me - Chưa đăng nhập trả về 401 Unauthorized")
    void getProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/v1/me - Đã đăng nhập trả về 200 OK cùng thông tin UserProfile")
    void getProfile_authenticated_returns200() throws Exception {
        CustomUserDetails principal = createUserPrincipal(10L);
        UserProfileResponse response = UserProfileResponse.builder()
                .id(10L)
                .email("user10@example.com")
                .fullName("Nguyen Van Ten")
                .phone("0987654321")
                .status("ACTIVE")
                .roles(Set.of("ROLE_USER"))
                .createdAt(Instant.now())
                .build();

        when(userService.getProfile(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/me").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.email").value("user10@example.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Ten"));

        verify(userService).getProfile(10L);
    }

    @Test
    @DisplayName("PATCH /api/v1/me - Cập nhật thông tin cá nhân trả về 200 OK")
    void updateProfile_authenticated_returns200() throws Exception {
        CustomUserDetails principal = createUserPrincipal(10L);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Nguyen Van Updated");
        request.setPhone("0912345678");

        UserProfileResponse response = UserProfileResponse.builder()
                .id(10L)
                .email("user10@example.com")
                .fullName("Nguyen Van Updated")
                .phone("0912345678")
                .status("ACTIVE")
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userService.updateProfile(eq(10L), any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/me")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Updated"));

        verify(userService).updateProfile(eq(10L), any(UpdateProfileRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/me/addresses - Lấy danh sách địa chỉ của người dùng hiện tại")
    void getAddresses_returns200() throws Exception {
        CustomUserDetails principal = createUserPrincipal(10L);
        AddressResponse addr = AddressResponse.builder()
                .id(1L)
                .userId(10L)
                .recipientName("Nguyen Van Ten")
                .phone("0987654321")
                .city("TP. Hồ Chí Minh")
                .isDefault(true)
                .build();

        when(addressService.getUserAddresses(10L)).thenReturn(List.of(addr));

        mockMvc.perform(get("/api/v1/me/addresses").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipientName").value("Nguyen Van Ten"));

        verify(addressService).getUserAddresses(10L);
    }

    @Test
    @DisplayName("POST /api/v1/me/addresses - Thêm mới địa chỉ trả về 201 Created")
    void createAddress_returns201() throws Exception {
        CustomUserDetails principal = createUserPrincipal(10L);
        CreateAddressRequest request = new CreateAddressRequest();
        request.setRecipientName("Nguyen Van Ten");
        request.setPhone("0987654321");
        request.setStreet("123 Nguyen Hue");
        request.setWard("Ben Nghe");
        request.setDistrict("Quan 1");
        request.setCity("TP. Ho Chi Minh");
        request.setIsDefault(true);

        AddressResponse addr = AddressResponse.builder()
                .id(2L)
                .userId(10L)
                .recipientName("Nguyen Van Ten")
                .phone("0987654321")
                .city("TP. Ho Chi Minh")
                .isDefault(true)
                .build();

        when(addressService.createAddress(eq(10L), any(CreateAddressRequest.class))).thenReturn(addr);

        mockMvc.perform(post("/api/v1/me/addresses")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$[0]").doesNotExist());

        verify(addressService).createAddress(eq(10L), any(CreateAddressRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/me/addresses/{id} - Xóa địa chỉ trả về 204 No Content")
    void deleteAddress_returns204() throws Exception {
        CustomUserDetails principal = createUserPrincipal(10L);

        mockMvc.perform(delete("/api/v1/me/addresses/2")
                        .with(user(principal)))
                .andExpect(status().isNoContent());

        verify(addressService).deleteAddress(10L, 2L);
    }
}
