package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.store.entities.LoginSessionEntity;
import io.github.pavelshe11.authmicro.store.entities.RefreshTokenSessionEntity;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.LoginSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RefreshTokenSessionRepository;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class SessionCleanerService {
    private final RegistrationSessionRepository registrationSessionRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    private final Long codeCleanTime;

    public SessionCleanerService(RegistrationSessionRepository registrationSessionRepository,
                                 LoginSessionRepository loginSessionRepository,
                                 RefreshTokenSessionRepository refreshTokenSessionRepository,
                                 @Value("${CODE_CLEAN_TIME}") Long codeCleanTime) {
        this.registrationSessionRepository = registrationSessionRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.codeCleanTime = codeCleanTime;
    }

    public void cleanLoginSession(LoginSessionEntity session) {
        loginSessionRepository.delete(session);
    }

    public void cleanRegistrationSession(RegistrationSessionEntity session) {
        registrationSessionRepository.delete(session);

    }

    public void cleanRefreshTokenSession(RefreshTokenSessionEntity session) {
        refreshTokenSessionRepository.delete(session);
    }

    @Scheduled(fixedRateString = "${REGISTRATION_LOGIN_SESSION_CLEAN_TIME}")
    @Transactional
    protected void cleanExpiredSession() {
        long now = System.currentTimeMillis();
        Timestamp fiveMinutesAgo = new Timestamp(now - codeCleanTime);

        registrationSessionRepository.deleteAllByCodeExpiresBefore(fiveMinutesAgo);
        loginSessionRepository.deleteAllByCodeExpiresBefore(fiveMinutesAgo);
    }

    @Scheduled(fixedRateString = "${REFRESH_TOKEN_SESSION_CLEAN_TIME}")
    @Transactional
    protected void cleanExpiredRefreshTokenSessions() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        refreshTokenSessionRepository.deleteAllByExpiresAtBefore(now);
    }
}
