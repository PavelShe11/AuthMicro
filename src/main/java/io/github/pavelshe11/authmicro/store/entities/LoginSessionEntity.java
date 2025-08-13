package io.github.pavelshe11.authmicro.store.entities;

import jakarta.persistence.*;
import jakarta.validation.Constraint;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "login_session", uniqueConstraints = {
        @UniqueConstraint(name = "uc_login_email_id", columnNames = {"email", "accountId"})
})
public class LoginSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(name = "code_expires", nullable = false)
    private Timestamp codeExpires;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt = Timestamp.from(Instant.now());
}
