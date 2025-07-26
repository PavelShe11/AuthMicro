package io.github.pavelshe11.authmicro.util;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;


@Component
public class CodeCache {
    private final Map<UUID, String> codeCache = new ConcurrentHashMap<>();

    public void save(UUID accountId, String code) {
        codeCache.put(accountId, code);
    }

    public Optional<String> get(UUID accountId) {
        return Optional.ofNullable(codeCache.get(accountId));
    }

    public void remove(UUID accountId) {
        codeCache.remove(accountId);
    }


}
