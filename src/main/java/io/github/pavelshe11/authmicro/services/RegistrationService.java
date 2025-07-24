package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.api.dto.responses.RegistrationResponseDto;
import io.github.pavelshe11.authmicro.api.exceptions.BadRequestException;
import io.github.pavelshe11.authmicro.api.exceptions.CodeVerificationException;
import io.github.pavelshe11.authmicro.api.exceptions.InvalidCodeException;
import io.github.pavelshe11.authmicro.store.entities.RegistrationSessionEntity;
import io.github.pavelshe11.authmicro.store.repositories.RegistrationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final RegistrationSessionRepository registrationSessionRepository;
    private final CodeGeneratorService registrationGeneratorService;

    public RegistrationResponseDto register(String email) {
//        if (email.trim().isEmpty()) {
//            throw new BadRequestException("Поле Email не может быть пустым.");
//        }

//        FindUserByEmailRequest request = FindUserByEmailRequest.newBuilder()
//                .setEmail(email)
//                .build();

//        FindUserByEmailResponse response = userServiceStub.findUserByEmail(request);

//        if (response.getExists()) {
//            throw new ServerAnswerException("Сервер не отвечает.");

        String code = registrationGeneratorService.codeGenerate();
        Instant codeExpires = registrationGeneratorService.codeExpiresGenerate();

        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findByEmail(email)
                .orElse(null);

        if (registrationSession != null) {
            if (registrationSession.getCodeExpires().isAfter(Instant.now())) {
                throw new BadRequestException("Код не истёк.");
            } else {
                registrationSession.setCode(registrationGeneratorService.CodeHash(code));
                registrationSession.setCodeExpires(codeExpires);
                registrationSessionRepository.save(registrationSession);

                return new RegistrationResponseDto(registrationSession.getId(), codeExpires, code);
            }
        }

        registrationSession = registrationSessionRepository.save(
                RegistrationSessionEntity.builder()
                        .email(email)
                        .acceptedPrivacyPolicy(true)
                        .acceptedPersonalDataProcessing(true)
                        .code(registrationGeneratorService.CodeHash(code))
                        .codeExpires(codeExpires)
                        .build()
        );

        return new RegistrationResponseDto(
                registrationSession.getId(),
                registrationSession.getCodeExpires(),
                code
        );
    }

    public ResponseEntity<Void> confirmEmail(UUID registrationId, String email, String code) {
        RegistrationSessionEntity registrationSession = registrationSessionRepository
                .findById(registrationId)
                .orElse(null);
        if (registrationSession == null) {
            return ResponseEntity.ok().build();
        }


        if (!passwordEncoder.matches(code, registrationSession.getCode())) {
            throw new InvalidCodeException("Неверный код подтверждения ");
        }

        if (registrationSession.getCodeExpires().isBefore(Instant.now())) {
            throw new CodeVerificationException("Код подтверждения истёк. Пожалуйста, запросите новый код и попробуйте снова.");
        }

        // Обрщение к сервису networking по grpc для создания юзера
        return ResponseEntity.ok().build();
    }
}