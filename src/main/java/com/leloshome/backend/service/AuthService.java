package com.leloshome.backend.service;

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
        return buildTokens(req.email());
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

        if (adminUserRepository.findByEmail(email).isEmpty()) {
            throw new BusinessException("Usuário não existe mais");
        }

        return buildTokens(email);
    }

    private LoginResponse buildTokens(String email) {
        return new LoginResponse(
                jwtService.generateAccessToken(email),
                jwtService.generateRefreshToken(email),
                jwtService.getAccessExpirationMs()
        );
    }
}
