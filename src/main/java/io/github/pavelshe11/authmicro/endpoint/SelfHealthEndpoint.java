package io.github.pavelshe11.authmicro.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "selfHealth")
public class SelfHealthEndpoint {

    private final Environment env;

    public SelfHealthEndpoint(Environment env) {
        this.env = env;
    }

    @ReadOperation
    public Map<String, Object> selfHealth() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", Instant.now().toString());
        result.put("service", env.getProperty("spring.application.name", "auth-service"));
        return result;
    }
}
