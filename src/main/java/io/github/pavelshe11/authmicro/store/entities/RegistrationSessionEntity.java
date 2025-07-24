package io.github.pavelshe11.authmicro.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "registration_session")
public class RegistrationSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "code_expires", nullable = false)
    private Instant codeExpires;

    @Builder.Default
    @Column(name = "is_accepted_privacy_policy", nullable = false)
    private Boolean acceptedPrivacyPolicy = true;

    @Builder.Default
    @Column(name = "is_accepted_personal_data_processing", nullable = false)
    private Boolean acceptedPersonalDataProcessing = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
