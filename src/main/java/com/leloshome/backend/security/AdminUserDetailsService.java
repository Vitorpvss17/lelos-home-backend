package com.leloshome.backend.security;

import com.leloshome.backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .map(admin -> User.builder()
                        .username(admin.getEmail())
                        .password(admin.getPassword())
                        .authorities(Collections.emptyList())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado: " + email));
    }
}
