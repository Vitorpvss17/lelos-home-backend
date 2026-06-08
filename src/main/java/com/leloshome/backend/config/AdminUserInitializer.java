package com.leloshome.backend.config;

import com.leloshome.backend.domain.AdminUser;
import com.leloshome.backend.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${admin.name:Admin}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD não configurados — seed de admin ignorado.");
            return;
        }

        if (repository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin inicial '{}' já existe — seed ignorado.", adminEmail);
            return;
        }

        AdminUser admin = AdminUser.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .name(adminName)
                .build();
        repository.save(admin);
        log.info("Admin inicial '{}' criado com sucesso.", adminEmail);
    }
}
