package io.github.pavelshe11.authmicro.services;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import net.devh.boot.grpc.client.inject.GrpcClient;

import io.github.pavelshe11.authmicro.grpc.AccountValidatorServiceGrpc;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmailValidatorGrpcService {
    @GrpcClient("email-validator-service")
    private AccountValidatorServiceGrpc.AccountValidatorServiceBlockingStub accountValidatorServiceBlockingStub;

    public boolean isAccountExists(String email) {
        AccountValidatorProto.CheckEmailRequest request = AccountValidatorProto.CheckEmailRequest.newBuilder()
                .setEmail(email)
                .setReturnAccountId(false)
                .build();
        AccountValidatorProto.CheckEmailResponse response = accountValidatorServiceBlockingStub.checkEmail(request);
        return response.getExists();
    }

    public Optional<String> getAccountIdIfExists(String email) {
        AccountValidatorProto.CheckEmailRequest request = AccountValidatorProto.CheckEmailRequest.newBuilder()
                .setEmail(email)
                .setReturnAccountId(true)
                .build();
        AccountValidatorProto.CheckEmailResponse response = accountValidatorServiceBlockingStub.checkEmail(request);
        return Optional.of(response.getAccountId());
    }
}
