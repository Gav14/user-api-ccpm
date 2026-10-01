package com.ccpm.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        LocalDateTime created,
        LocalDateTime modified,
        @JsonProperty("last_login") LocalDateTime lastLogin,
        String token,
        boolean isactive,
        String name,
        String email,
        List<PhoneDto> phones) {}
