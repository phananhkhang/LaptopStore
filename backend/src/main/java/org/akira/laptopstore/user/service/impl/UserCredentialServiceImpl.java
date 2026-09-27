package org.akira.laptopstore.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.exception.BusinessException;
import org.akira.laptopstore.user.dto.UserCredentialDto;
import org.akira.laptopstore.user.entity.Role;
import org.akira.laptopstore.user.entity.User;
import org.akira.laptopstore.user.repository.RoleRepository;
import org.akira.laptopstore.user.repository.UserRepository;
import org.akira.laptopstore.user.service.UserCredentialService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCredentialServiceImpl implements UserCredentialService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentialDto> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCredentialDto> findById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public UserCredentialDto createUser(String email, String passwordHash, String fullName, String phone, Set<String> roleNames) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Email already registered: " + normalizedEmail);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
            roles.add(role);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .fullName(fullName)
                .phone(phone)
                .tokenVersion(0)
                .status("ACTIVE")
                .roles(roles)
                .build();

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void incrementTokenVersion(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
    }

    private UserCredentialDto toDto(User user) {
        return UserCredentialDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .tokenVersion(user.getTokenVersion())
                .status(user.getStatus())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
