package io.github.pavelshe11.authmicro.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    public String codeGenerate() {
        String code = String.format("%06d", new Random().nextInt(1000000));
        return code;
    }

    public Instant codeExpiresGenerate() {
        return Instant.now().plus(5, ChronoUnit.MINUTES);
    }
}
