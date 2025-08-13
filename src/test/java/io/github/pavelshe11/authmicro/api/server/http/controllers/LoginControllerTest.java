package io.github.pavelshe11.authmicro.api.server.http.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pavelshe11.authmicro.api.client.grpc.GetAccountInfoGrpc;
import io.github.pavelshe11.authmicro.api.dto.requests.LoginRequestDto;
import io.github.pavelshe11.authmicro.api.dto.responses.LoginResponseDto;
import io.github.pavelshe11.authmicro.config.TestSecurityConfig;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc // для ответов на уровне контроллера и http
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoginSessionRepository loginSessionRepository;

    @MockitoBean
    private GetAccountInfoGrpc getAccountInfoGrpc;

    @Test
    @Sql(scripts = "/data/cleanUp.sql")
    void sendLoginCodeFakePositiveTest() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("test1@communicator.ru");
        when(getAccountInfoGrpc.getAccountInfoByEmail("test1@communicator.ru"))
                .thenReturn(Optional.empty());
        String loginJson = objectMapper.writeValueAsString(loginRequestDto);


        MvcResult result = mockMvc.perform(post("/auth/v1/login/sendCodeEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeExpires").exists())
                .andExpect(jsonPath("$.codePattern").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        LoginResponseDto response = objectMapper.readValue(responseBody, LoginResponseDto.class);

        Assertions.assertTrue(response.getCodeExpires() > System.currentTimeMillis());
        Assertions.assertNotNull(response.getCodePattern());

        boolean exists = loginSessionRepository.existsByEmail("test1@communicator.ru");
        Assertions.assertTrue(exists);
    }

//    @Test
//    @Sql(scripts = "/data/cleanUp.sql")
//    void sendLoginCodeNegativeTest() {
//    }
//
//    @Test
//    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
//    void confirmLoginEmail() {
//    }
}