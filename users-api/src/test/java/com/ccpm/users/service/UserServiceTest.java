package com.ccpm.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ccpm.users.config.AppProperties;
import com.ccpm.users.dto.PhoneDto;
import com.ccpm.users.dto.UserRequest;
import com.ccpm.users.dto.UserResponse;
import com.ccpm.users.exception.EmailAlreadyExistsException;
import com.ccpm.users.exception.InvalidRequestException;
import com.ccpm.users.model.User;
import com.ccpm.users.repository.UserRepository;
import com.ccpm.users.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserServiceTest {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+([.][A-Za-z0-9-]+)*[.][A-Za-z]{2,}$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*[0-9]).{6,}$";

    private UserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        AppProperties props = new AppProperties(
                new AppProperties.Validation(EMAIL_REGEX, PASSWORD_REGEX),
                new AppProperties.Jwt("secreto-de-pruebas-con-mas-de-32-caracteres", 60));
        service = new UserService(repository, new UserRequestValidator(props), new UserMapper(),
                new BCryptPasswordEncoder(), new JwtService(props));
        when(repository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private UserRequest validRequest() {
        return new UserRequest("Juan Rodriguez", "juan@rodriguez.org", "hunter2",
                List.of(new PhoneDto("1234567", "1", "57")));
    }

    @Test
    void registraUsuarioNuevo() {
        when(repository.existsByEmail("juan@rodriguez.org")).thenReturn(false);

        UserResponse response = service.register(validRequest());

        assertEquals(response.created(), response.lastLogin());
        assertEquals(response.created(), response.modified());
        assertTrue(response.isactive());
        assertNotNull(response.token());
        assertEquals(1, response.phones().size());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).saveAndFlush(captor.capture());
        assertNotEquals("hunter2", captor.getValue().getPassword(), "la contraseña debe guardarse con hash");
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(repository.existsByEmail("juan@rodriguez.org")).thenReturn(true);

        EmailAlreadyExistsException ex =
                assertThrows(EmailAlreadyExistsException.class, () -> service.register(validRequest()));

        assertEquals("El correo ya registrado", ex.getMessage());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rechazaCorreoConFormatoInvalido() {
        UserRequest request = new UserRequest("Juan", "no-es-un-correo", "hunter2", List.of());
        assertThrows(InvalidRequestException.class, () -> service.register(request));
    }

    @Test
    void rechazaContrasenaQueNoCumpleLaRegex() {
        UserRequest request = new UserRequest("Juan", "juan@rodriguez.org", "abc", List.of());
        assertThrows(InvalidRequestException.class, () -> service.register(request));
    }
}
