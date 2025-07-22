package io.github.pavelshe11.authmicro.store.repositories;

import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSessionEntity, UUID> {
}
