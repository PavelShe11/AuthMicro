package io.github.pavelshe11.authmicro.store.repositories;

import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginSessionRepository extends JpaRepository<LoginSessionEntity, UUID> {
    Optional<LoginSessionEntity> findByCode(String code);

    boolean getByIdAndEmail(UUID id, String email);

    Optional<LoginSessionEntity> findByAccountIdAndEmail(UUID accountId, String email);
}
