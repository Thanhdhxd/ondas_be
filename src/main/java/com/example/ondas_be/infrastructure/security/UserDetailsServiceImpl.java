package com.example.ondas_be.infrastructure.security;

import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepoPort userRepoPort;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        GrantedAuthority authority = new SimpleGrantedAuthority(
                user.getRole() != null ? user.getRole().getAuthority() : "ROLE_USER");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(),       // enabled
                true,                  // accountNonExpired
                true,                  // credentialsNonExpired
                true,                  // accountNonLocked
                List.of(authority)
        );
    }
}
