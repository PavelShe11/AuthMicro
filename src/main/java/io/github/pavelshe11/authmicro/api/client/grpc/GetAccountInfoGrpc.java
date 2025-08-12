package io.github.pavelshe11.authmicro.api.client.grpc;

import io.github.pavelshe11.authmicro.grpc.GetAccountInfoServiceGrpc;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class GetAccountInfoGrpc {
//    @GrpcClient("get-account-info-service")
    private final GetAccountInfoServiceGrpc.GetAccountInfoServiceBlockingStub getAccountInfoServiceBlockingStub;

    public Optional<getAccountInfoProto.GetAccountInfoResponse> getAccountInfoByEmail(String email) {
        getAccountInfoProto.GetAccountInfoByEmailRequest request =
                getAccountInfoProto.GetAccountInfoByEmailRequest.newBuilder()
                        .setEmail(email)
                        .build();
        getAccountInfoProto.GetAccountInfoResponse response =
                getAccountInfoServiceBlockingStub.getAccountInfoByEmail(request);

        if (response.getUserDataMap().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(response);
    }

    public Optional<getAccountInfoProto.GetAccountInfoResponse> getAccountInfoById(String accountId) {
        getAccountInfoProto.GetAccountByIdRequest request =
                getAccountInfoProto.GetAccountByIdRequest.newBuilder()
                        .setAccountId(accountId)
                        .build();

        getAccountInfoProto.GetAccountInfoResponse response =
                getAccountInfoServiceBlockingStub.getAccountById(request);

        if (response.getUserDataMap().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(response);
    }
}
