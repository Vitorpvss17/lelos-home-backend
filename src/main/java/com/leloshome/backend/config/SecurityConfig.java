package com.leloshome.backend.config;

import com.leloshome.backend.security.AdminUserDetailsService;
import com.leloshome.backend.security.JwtAuthenticationFilter;
import com.leloshome.backend.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AdminUserDetailsService userDetailsService;

    /**
     * Origens permitidas pelo CORS, separadas por vírgula (suporta padrões, ex.: http://localhost:*).
     * Dev: "*"; produção: defina CORS_ALLOWED_ORIGINS com o(s) domínio(s) do front.
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    // Rate limiting (anti brute-force / spam). Desativável via ratelimit.enabled=false (perfil de teste).
    @Value("${ratelimit.enabled:true}")
    private boolean rateLimitEnabled;
    @Value("${ratelimit.login.capacity:5}")
    private int loginCapacity;
    @Value("${ratelimit.login.refill-seconds:60}")
    private long loginRefillSeconds;
    @Value("${ratelimit.orders.capacity:20}")
    private int ordersCapacity;
    @Value("${ratelimit.orders.refill-seconds:60}")
    private long ordersRefillSeconds;

    private RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter(rateLimitEnabled, List.of(
                new RateLimitingFilter.Rule("POST", "/api/admin/auth/login", loginCapacity, loginRefillSeconds),
                new RateLimitingFilter.Rule("POST", "/api/orders", ordersCapacity, ordersRefillSeconds)
        ));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Swagger
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Actuator (health/info públicos para probes do Railway)
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                // Public API
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/kits/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/orders/**").permitAll()
                // Admin auth
                .requestMatchers("/api/admin/auth/**").permitAll()
                // Everything else requires auth
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authenticationProvider(authenticationProvider())
            // rate limit primeiro, depois o filtro JWT (ambos antes da auth por usuário/senha)
            .addFilterBefore(rateLimitingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        // allowedOriginPatterns aceita tanto origens exatas quanto padrões (ex.: http://localhost:*)
        config.setAllowedOriginPatterns(origins.isEmpty() ? List.of("*") : origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        // API stateless por Bearer token — não usamos cookies, então sem credenciais.
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
