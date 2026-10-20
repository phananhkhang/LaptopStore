package org.akira.laptopstore.user.service;

import org.akira.laptopstore.exception.ResourceNotFoundException;
import org.akira.laptopstore.user.dto.ChangeUserStatusRequest;
import org.akira.laptopstore.user.dto.UpdateProfileRequest;
import org.akira.laptopstore.user.dto.UserProfileResponse;
import org.akira.laptopstore.user.entity.Role;
import org.akira.laptopstore.user.entity.User;
import org.akira.laptopstore.user.repository.RoleRepository;
import org.akira.laptopstore.user.repository.UserRepository;
import org.akira.laptopstore.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("ROLE_USER").build();
        sampleUser = User.builder()
                .id(100L)
                .email("user100@laptopstore.com")
                .fullName("Nguyen Van A")
                .phone("0987654321")
                .status("ACTIVE")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
    }

    @Test
    @DisplayName("getProfile: Should return user profile when user exists")
    void getProfile_whenExists_shouldReturnProfile() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));

        UserProfileResponse response = userService.getProfile(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getEmail()).isEqualTo("user100@laptopstore.com");
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(response.getRoles()).contains("ROLE_USER");
    }

    @Test
    @DisplayName("getProfile: Should throw ResourceNotFoundException when user does not exist")
    void getProfile_whenNotFound_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("updateProfile: Should update full name and phone and save")
    void updateProfile_shouldUpdateFields() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Nguyen Van B");
        request.setPhone("0912345678");

        UserProfileResponse response = userService.updateProfile(100L, request);

        assertThat(response.getFullName()).isEqualTo("Nguyen Van B");
        assertThat(response.getPhone()).isEqualTo("0912345678");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("updateUserStatus: Should update status and save user")
    void updateUserStatus_shouldUpdateStatus() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangeUserStatusRequest request = new ChangeUserStatusRequest();
        request.setStatus("SUSPENDED");

        UserProfileResponse response = userService.updateUserStatus(100L, request);

        assertThat(response.getStatus()).isEqualTo("SUSPENDED");
        verify(userRepository).save(sampleUser);
    }
}
