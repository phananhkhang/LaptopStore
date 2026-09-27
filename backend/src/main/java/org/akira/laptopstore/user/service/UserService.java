package org.akira.laptopstore.user.service;

import org.akira.laptopstore.user.dto.ChangeUserRolesRequest;
import org.akira.laptopstore.user.dto.ChangeUserStatusRequest;
import org.akira.laptopstore.user.dto.UpdateProfileRequest;
import org.akira.laptopstore.user.dto.UserProfileResponse;

import java.util.List;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    List<UserProfileResponse> getAllUsers();
    UserProfileResponse updateUserStatus(Long userId, ChangeUserStatusRequest request);
    UserProfileResponse updateUserRoles(Long userId, ChangeUserRolesRequest request);
}
