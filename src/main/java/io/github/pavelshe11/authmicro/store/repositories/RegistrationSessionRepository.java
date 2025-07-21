package io.github.pavelshe11.authmicro.store.repositories;

import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistrationSessionRepository extends JpaRepository<RegistrationSessionEntity, UUID> {
}
