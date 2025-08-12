package io.github.pavelshe11.authmicro.api.server.http.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pavelshe11.authmicro.api.dto.requests.LoginRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc // для ответов на уровне контроллера и http
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void sendLoginCodePositiveTest() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("test1@communicator.ru");
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void sendLoginCodeNegativeTest() {
    }

    @Test
    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
    void confirmLoginEmail() {
    }
}