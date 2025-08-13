package io.github.pavelshe11.authmicro.config;


import com.google.gson.Gson;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.grpc.AccountCreationServiceGrpc;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorServiceGrpc;
import io.github.pavelshe11.authmicro.grpc.GetAccountInfoServiceGrpc;
import io.github.pavelshe11.authmicro.services.RegistrationService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
public class GrpcClientConfig {
    private static final Logger log = LoggerFactory.getLogger(GrpcClientConfig.class);

    @Value("${GRPC_SERVICE_ADDRESS}")
    private String grpcServiceAddress;

    private Map<String, ?> loadRetryServiceConfig() {
        Gson gson = new Gson();
        var inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("grpc-config.json");

        if (inputStream == null) {
            throw new ServerAnswerException();
        }

        return gson.fromJson(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                Map.class
        );
    }

    @Bean
    public ManagedChannel grpcChannel() {
        return createChannel(grpcServiceAddress);
    }

    @Bean
    public AccountCreationServiceGrpc.AccountCreationServiceBlockingStub accountCreationStub(ManagedChannel grpcChannel) {
        return AccountCreationServiceGrpc.newBlockingStub(grpcChannel);
    }

    @Bean
    public AccountValidatorServiceGrpc.AccountValidatorServiceBlockingStub accountValidatorStub(ManagedChannel grpcChannel) {
        return AccountValidatorServiceGrpc.newBlockingStub(grpcChannel);
    }

    @Bean
    public GetAccountInfoServiceGrpc.GetAccountInfoServiceBlockingStub getAccountInfoStub(ManagedChannel grpcChannel) {
        return GetAccountInfoServiceGrpc.newBlockingStub(grpcChannel);
    }

    private ManagedChannel createChannel(String target) {
        try {
            return ManagedChannelBuilder
                    .forTarget(target)
                    .defaultServiceConfig(loadRetryServiceConfig())
                    .enableRetry()
                    .usePlaintext()
                    .build();

        } catch (Exception e) {
            log.warn("Нет подключения к networking");
            throw new ServerAnswerException();
        }
    }
}