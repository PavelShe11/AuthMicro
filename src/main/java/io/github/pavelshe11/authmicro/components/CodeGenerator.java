package io.github.pavelshe11.authmicro.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class CodeGenerator {
    @Value("${code.lifetime.minutes}")
    private int codeLifetimeMinutes;

    private final PasswordEncoder passwordEncoder;

    public String codeGenerate() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    public String codeHash(String code) {
        return passwordEncoder.encode(code);
    }

    public long codeExpiresGenerate() {
        return Instant.now().plus(codeLifetimeMinutes, ChronoUnit.MINUTES).toEpochMilli();
    }
}
