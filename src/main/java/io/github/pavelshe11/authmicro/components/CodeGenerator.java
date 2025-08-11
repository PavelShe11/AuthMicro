package io.github.pavelshe11.authmicro.components;

import com.github.curiousoddman.rgxgen.RgxGen;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Component
public class CodeGenerator {

    private final int codeLifetimeMinutes;
    private final String codePattern;
    private final PasswordEncoder passwordEncoder;

    public CodeGenerator(
            @Value("${code.lifetime.minutes}") int codeLifetimeMinutes,
            @Value("${CODE_PATTERN}") String codePattern,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
        this.codePattern = codePattern;
        this.codeLifetimeMinutes = codeLifetimeMinutes;
    }

    public String codeGenerate() {
        RgxGen rgxGen = RgxGen.parse(codePattern);
        String stringCode = rgxGen.generate();
        return stringCode;
    }

    public String codeHash(String code) {
        return passwordEncoder.encode(code);
    }

    public long codeExpiresGenerate() {
        return Instant.now().plus(codeLifetimeMinutes, ChronoUnit.MINUTES).toEpochMilli();
    }
}
