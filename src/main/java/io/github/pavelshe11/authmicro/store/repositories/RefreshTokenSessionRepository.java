package io.github.pavelshe11.authmicro.store.repositories;

import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSessionEntity, UUID> {
    boolean existsByRefreshToken(String refreshToken);

    Optional<RefreshTokenSessionEntity> findByRefreshToken(String refreshToken);

    void deleteAllByExpiresAtBefore(Timestamp now);
}
