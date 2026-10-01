package com.ccpm.users.service;

import com.ccpm.users.config.AppProperties;
import com.ccpm.users.dto.PhoneDto;
import com.ccpm.users.dto.UserRequest;
import com.ccpm.users.exception.InvalidRequestException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Valida el request. Las regex vienen de application.properties, por eso son configurables. */
@Component
public class UserRequestValidator {

    private static final int BCRYPT_MAX_BYTES = 72;

    private final Pattern emailPattern;
    private final Pattern passwordPattern;

    public UserRequestValidator(AppProperties props) {
        this.emailPattern = Pattern.compile(props.validation().emailRegex());
        this.passwordPattern = Pattern.compile(props.validation().passwordRegex());
    }

    public void validate(UserRequest request) {
        List<String> errors = new ArrayList<>();

        if (isBlank(request.name())) {
            errors.add("El nombre es obligatorio");
        }

        if (isBlank(request.email())) {
            errors.add("El correo es obligatorio");
        } else if (!emailPattern.matcher(request.email().trim()).matches()) {
            errors.add("El correo no tiene un formato válido (ej: aaaaaaa@dominio.cl)");
        }

        if (isBlank(request.password())) {
            errors.add("La contraseña es obligatoria");
        } else if (!passwordPattern.matcher(request.password()).matches()) {
            errors.add("La contraseña no cumple el formato requerido");
        } else if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            errors.add("La contraseña no puede superar los 72 bytes");
        }

        if (request.phones() != null) {
            for (int i = 0; i < request.phones().size(); i++) {
                PhoneDto p = request.phones().get(i);
                if (p == null || isBlank(p.number()) || isBlank(p.citycode()) || isBlank(p.contrycode())) {
                    errors.add("El teléfono en la posición " + i
                            + " requiere number, citycode y contrycode");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(String.join("; ", errors));
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
