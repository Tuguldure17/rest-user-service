package com.soa.rest.config;

import com.soa.rest.middleware.AuthMiddleware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS болон AuthMiddleware тохиргоо.
 */
@Configuration
public class AppConfig {

    @Value("${cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500}")
    private String allowedOrigins;

    // ── CORS Filter ───────────────────────────────────────────
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // ── Auth Middleware – /users/** бүх хаягт хэрэглэх ──────
    @Bean
    public FilterRegistrationBean<AuthMiddleware> authFilter(AuthMiddleware middleware) {
        FilterRegistrationBean<AuthMiddleware> bean = new FilterRegistrationBean<>();
        bean.setFilter(middleware);
        bean.addUrlPatterns("/users/*", "/users");
        bean.setOrder(2);
        return bean;
    }
}