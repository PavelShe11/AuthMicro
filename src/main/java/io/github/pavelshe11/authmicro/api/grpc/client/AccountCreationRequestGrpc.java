package io.github.pavelshe11.authmicro.api.grpc.client;

import io.github.pavelshe11.authmicro.grpc.AccountCreationProto;
import io.github.pavelshe11.authmicro.grpc.AccountCreationServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class AccountCreationRequestGrpc {
    @GrpcClient("account-creation-service")
    private AccountCreationServiceGrpc.AccountCreationServiceBlockingStub accountCreationServiceBlockingStub;

    public boolean createAccount(String email) {
        try {
            AccountCreationProto.CreateAccountRequest request =
                    AccountCreationProto.CreateAccountRequest.newBuilder()
                            .setEmail(email)
                            .build();
            AccountCreationProto.CreateAccountResponse response = accountCreationServiceBlockingStub.createAccount(request);

            return response.hasSuccess();
        } catch (Exception e) {
            return false;
        }
    }
}
