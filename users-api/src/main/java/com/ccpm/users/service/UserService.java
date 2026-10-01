package com.ccpm.users.service;

import com.ccpm.users.dto.UserRequest;
import com.ccpm.users.dto.UserResponse;
import com.ccpm.users.exception.EmailAlreadyExistsException;
import com.ccpm.users.model.User;
import com.ccpm.users.repository.UserRepository;
import com.ccpm.users.security.JwtService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRequestValidator validator;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       UserRequestValidator validator,
                       UserMapper mapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(UserRequest request) {
        // 1. Validar formato
        validator.validate(request);

        // 2. Normalizar el correo y verificar duplicado
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        // 3. Construir la entidad. Una sola "now" para que created == modified == last_login
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setToken(jwtService.generateToken(email));
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        user.setActive(true);
        if (request.phones() != null) {
            request.phones().forEach(p -> user.addPhone(mapper.toPhone(p)));
        }

        // 4. Persistir (usuario + teléfonos + token en la misma transacción)
        return mapper.toResponse(userRepository.saveAndFlush(user));
    }
}
