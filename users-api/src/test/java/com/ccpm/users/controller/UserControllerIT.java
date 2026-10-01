package com.ccpm.users.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Prueba de integración: levanta todo Spring + H2 y llama al endpoint real. */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private static String body(String email, String password) {
        return """
                {"name":"Juan Rodriguez","email":"%s","password":"%s",
                 "phones":[{"number":"1234567","citycode":"1","contrycode":"57"}]}
                """.formatted(email, password);
    }

    @Test
    void creaUsuarioYRetorna201() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(body("nuevo@dominio.cl", "hunter2")))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.last_login").isNotEmpty())
                .andExpect(jsonPath("$.isactive").value(true))
                .andExpect(jsonPath("$.phones[0].contrycode").value("57"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void correoDuplicadoRetorna409() throws Exception {
        String json = body("duplicado@dominio.cl", "hunter2");
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    @Test
    void correoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(body("correo-malo", "hunter2")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void contrasenaInvalidaRetorna400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(body("otro@dominio.cl", "abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void jsonMalFormadoRetorna400ConFormatoDeError() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }
}
