package io.github.pavelshe11.authmicro.api.client.grpc;

import io.github.pavelshe11.authmicro.grpc.GetAccountInfoServiceGrpc;
import io.github.pavelshe11.authmicro.grpc.getAccountInfoProto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAccountInfoGrpc {
    @GrpcClient("get-account-info-service")
    private GetAccountInfoServiceGrpc.GetAccountInfoServiceBlockingStub getAccountInfoServiceBlockingStub;

    public Optional<getAccountInfoProto.GetAccountInfoResponse> getAccountInfo(String email) {
        getAccountInfoProto.GetAccountInfoRequest request =
                getAccountInfoProto.GetAccountInfoRequest.newBuilder()
                        .setEmail(email)
                        .build();
        getAccountInfoProto.GetAccountInfoResponse response = getAccountInfoServiceBlockingStub.getAccountInfo(request);

        if (response.getAccountId() == null || response.getAccountId().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(response);
    }

    public boolean checkIfAccountExistsById(String accountId) {
        getAccountInfoProto.CheckAccountByIdRequest request =
                getAccountInfoProto.CheckAccountByIdRequest.newBuilder()
                        .setAccountId(accountId)
                        .build();

        getAccountInfoProto.CheckAccountByIdResponse response = getAccountInfoServiceBlockingStub.getAccountById(request);

        return response.getAccept();
    }
}
