package com.ccpm.users.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración externa (application.properties) con prefijo "app". */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Validation validation, Jwt jwt) {

    public record Validation(String emailRegex, String passwordRegex) {}

    public record Jwt(String secret, long expirationMinutes) {}
}
