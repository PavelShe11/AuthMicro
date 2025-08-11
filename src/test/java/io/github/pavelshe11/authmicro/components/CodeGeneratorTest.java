package io.github.pavelshe11.authmicro.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorUnitTest {

    private CodeGenerator codeGenerator;

    @BeforeEach
    void setUp() {
        int codeLifetimeMinutes = 5;
        String codePattern = "[0-9]{6}"; // пример шаблона
        var passwordEncoder = new BCryptPasswordEncoder();

        codeGenerator = new CodeGenerator(codeLifetimeMinutes, codePattern, passwordEncoder);
    }

    @Test
    void testCodeGenerate() {
        String code = codeGenerator.codeGenerate();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void testCodeHash() {
        String rawCode = "123456";
        String hashed = codeGenerator.codeHash(rawCode);

        assertNotEquals(rawCode, hashed);
        assertTrue(new BCryptPasswordEncoder().matches(rawCode, hashed));
    }

    @Test
    void testCodeExpiresGenerate() {
        long expiresAt = codeGenerator.codeExpiresGenerate();
        assertTrue(expiresAt > System.currentTimeMillis());
    }
}