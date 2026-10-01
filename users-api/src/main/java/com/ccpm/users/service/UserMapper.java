package com.ccpm.users.service;

import com.ccpm.users.dto.PhoneDto;
import com.ccpm.users.dto.UserResponse;
import com.ccpm.users.model.Phone;
import com.ccpm.users.model.User;
import java.util.List;
import org.springframework.stereotype.Component;

/** Convierte entre entidades (BD) y DTOs (JSON). Así la contraseña nunca sale en la respuesta. */
@Component
public class UserMapper {

    public Phone toPhone(PhoneDto dto) {
        Phone phone = new Phone();
        phone.setNumber(dto.number().trim());
        phone.setCityCode(dto.citycode().trim());
        phone.setCountryCode(dto.contrycode().trim());
        return phone;
    }

    public UserResponse toResponse(User user) {
        List<PhoneDto> phones = user.getPhones().stream()
                .map(p -> new PhoneDto(p.getNumber(), p.getCityCode(), p.getCountryCode()))
                .toList();
        return new UserResponse(
                user.getId(),
                user.getCreated(),
                user.getModified(),
                user.getLastLogin(),
                user.getToken(),
                user.isActive(),
                user.getName(),
                user.getEmail(),
                phones);
    }
}
