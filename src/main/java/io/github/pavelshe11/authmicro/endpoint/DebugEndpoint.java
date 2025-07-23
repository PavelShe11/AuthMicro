package io.github.pavelshe11.authmicro.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "debug")
public class DebugEndpoint {

    private final Environment env;

    public DebugEndpoint(Environment env) {
        this.env = env;
    }

    @ReadOperation
    public Map<String, Object> debug() {
        Map<String, Object> result = new HashMap<>();

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (attrs != null) ? attrs.getRequest() : null;

        result.put("service", env.getProperty("spring.application.name"));
        result.put("timestamp", Instant.now().toString());

        if (request != null) {
            result.put("client_ip", request.getHeader("X-Forwarded-For"));
            result.put("host", request.getHeader("Host"));
            result.put("method", request.getMethod());
            result.put("uri", request.getRequestURI());
        } else {
            result.put("client_ip", "N/A");
            result.put("host", "N/A");
            result.put("method", "N/A");
            result.put("uri", "N/A");
        }

        return result;
    }
}
