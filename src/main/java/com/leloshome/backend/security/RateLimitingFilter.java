package com.leloshome.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting por IP (token bucket em memória) para endpoints sensíveis:
 * login (anti brute-force) e criação pública de pedidos (anti spam/DoS).
 * Sem dependência externa; adequado a deploy single-instance (Railway).
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    /** Limite para um endpoint específico. */
    public record Rule(String method, String path, int capacity, long refillSeconds) {
        boolean matches(HttpServletRequest req) {
            return method.equalsIgnoreCase(req.getMethod()) && path.equals(req.getRequestURI());
        }
    }

    private static final int MAX_KEYS = 100_000;

    private final boolean enabled;
    private final List<Rule> rules;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(boolean enabled, List<Rule> rules) {
        this.enabled = enabled;
        this.rules = rules;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        Rule rule = rules.stream().filter(r -> r.matches(request)).findFirst().orElse(null);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }
        evictIfTooLarge();
        String key = rule.path() + "|" + clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(rule.capacity(), rule.refillSeconds()));

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            tooManyRequests(request, response, rule.refillSeconds());
        }
    }

    private void tooManyRequests(HttpServletRequest req, HttpServletResponse res, long retryAfter) throws IOException {
        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
        res.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"Muitas requisições. Tente novamente em instantes.\",\"path\":\"%s\"}",
                Instant.now(), req.getRequestURI()));
    }

    private String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    /** Evita crescimento ilimitado do mapa sob ataque distribuído: remove buckets ociosos (cheios). */
    private void evictIfTooLarge() {
        if (buckets.size() <= MAX_KEYS) return;
        buckets.values().removeIf(Bucket::isFull);
    }

    /** Token bucket com refill contínuo. */
    private static final class Bucket {
        private final double capacity;
        private final double refillPerNano;
        private double tokens;
        private long lastNanos;

        Bucket(int capacity, long refillSeconds) {
            this.capacity = capacity;
            this.refillPerNano = capacity / (refillSeconds * 1_000_000_000.0);
            this.tokens = capacity;
            this.lastNanos = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            long now = System.nanoTime();
            tokens = Math.min(capacity, tokens + (now - lastNanos) * refillPerNano);
            lastNanos = now;
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        synchronized boolean isFull() {
            long now = System.nanoTime();
            double current = Math.min(capacity, tokens + (now - lastNanos) * refillPerNano);
            return current >= capacity;
        }
    }
}
