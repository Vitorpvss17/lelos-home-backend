package com.leloshome.backend.service;

import com.leloshome.backend.domain.AdminUser;
import com.leloshome.backend.dto.request.LoginRequest;
import com.leloshome.backend.dto.request.RefreshRequest;
import com.leloshome.backend.dto.response.LoginResponse;
import com.leloshome.backend.exception.BusinessException;
import com.leloshome.backend.repository.AdminUserRepository;
import com.leloshome.backend.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AdminUserRepository adminUserRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }
        AdminUser user = adminUserRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
        return buildTokens(user);
    }

    public LoginResponse refresh(RefreshRequest req) {
        String token = req.refreshToken();

        String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Refresh token inválido");
        }

        if (!jwtService.isRefreshToken(token) || !jwtService.isValid(token, email)) {
            throw new BadCredentialsException("Refresh token inválido ou expirado");
        }

        AdminUser user = adminUserRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não existe mais"));

        // Sessão revogada (logout/troca de senha): a versão do token não bate mais.
        Integer tokenVersion = jwtService.extractTokenVersion(token);
        if (tokenVersion == null || tokenVersion != user.getTokenVersion()) {
            throw new BadCredentialsException("Sessão revogada. Faça login novamente.");
        }

        return buildTokens(user);
    }

    /** Revoga (no servidor) todos os tokens do admin incrementando o tokenVersion. */
    @Transactional
    public void logout(String email) {
        adminUserRepository.findByEmail(email).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            adminUserRepository.save(user);
        });
    }

    private LoginResponse buildTokens(AdminUser user) {
        int version = user.getTokenVersion();
        return new LoginResponse(
                jwtService.generateAccessToken(user.getEmail(), version),
                jwtService.generateRefreshToken(user.getEmail(), version),
                jwtService.getAccessExpirationMs()
        );
    }
}
