package io.github.pavelshe11.authmicro.services;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import net.devh.boot.grpc.client.inject.GrpcClient;

import io.github.pavelshe11.authmicro.grpc.AccountValidatorServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class EmailValidatorService {
    @GrpcClient("email-validator-service")
    private AccountValidatorServiceGrpc.AccountValidatorServiceBlockingStub accountValidatorServiceBlockingStub;

    public boolean isAccountExists(String email) {
        AccountValidatorProto.CheckEmailRequest request = AccountValidatorProto.CheckEmailRequest.newBuilder()
                .setEmail(email)
                .build();
        AccountValidatorProto.CheckEmailResponse response = accountValidatorServiceBlockingStub.checkEmail(request);
        return response.getExists();
    }
}
