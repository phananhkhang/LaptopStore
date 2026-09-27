package org.akira.laptopstore.security.principal;

import lombok.RequiredArgsConstructor;
import org.akira.laptopstore.user.dto.UserCredentialDto;
import org.akira.laptopstore.user.service.UserCredentialService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialService userCredentialService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredentialDto credential = userCredentialService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<SimpleGrantedAuthority> authorities = credential.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new CustomUserDetails(
                credential.getId(),
                credential.getEmail(),
                credential.getPasswordHash(),
                credential.getTokenVersion(),
                credential.getStatus(),
                authorities
        );
    }

    public UserDetails loadUserById(Long id) {
        UserCredentialDto credential = userCredentialService.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        List<SimpleGrantedAuthority> authorities = credential.getRoles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new CustomUserDetails(
                credential.getId(),
                credential.getEmail(),
                credential.getPasswordHash(),
                credential.getTokenVersion(),
                credential.getStatus(),
                authorities
        );
    }
}
