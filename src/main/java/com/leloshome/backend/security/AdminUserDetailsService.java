package com.leloshome.backend.security;

import com.leloshome.backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .map(admin -> new AdminUserDetails(
                        admin.getEmail(),
                        admin.getPassword(),
                        admin.getTokenVersion() != null ? admin.getTokenVersion() : 0))
                .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado: " + email));
    }
}
