package org.akira.laptopstore.user.service;

import org.akira.laptopstore.user.dto.UserCredentialDto;

import java.util.Optional;
import java.util.Set;

public interface UserCredentialService {
    Optional<UserCredentialDto> findByEmail(String email);
    Optional<UserCredentialDto> findById(Long id);
    UserCredentialDto createUser(String email, String passwordHash, String fullName, String phone, Set<String> roleNames);
    void incrementTokenVersion(Long userId);
}
