package com.leloshome.backend.security;

import org.springframework.security.core.userdetails.User;

import java.util.Collections;

/**
 * UserDetails do admin que carrega o tokenVersion atual, para o filtro JWT
 * comparar com o claim do token e rejeitar tokens revogados.
 */
public class AdminUserDetails extends User {

    private final int tokenVersion;

    public AdminUserDetails(String username, String password, int tokenVersion) {
        super(username, password, Collections.emptyList());
        this.tokenVersion = tokenVersion;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }
}
