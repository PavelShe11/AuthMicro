package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.grpc.CheckIsAdminProto;
import io.github.pavelshe11.authmicro.grpc.CheckIsAdminServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoleResolverGrpcService {

    @GrpcClient("role-resolver-service")
    private CheckIsAdminServiceGrpc.CheckIsAdminServiceBlockingStub checkIsAdminServiceBlockingStub;

    public boolean isAdmin(UUID accountId) {
        CheckIsAdminProto.CheckIsAdminRequest request = CheckIsAdminProto.CheckIsAdminRequest
                .newBuilder()
                .setAccountId(accountId.toString())
                .build();

        CheckIsAdminProto.CheckIsAdminResponse response = checkIsAdminServiceBlockingStub.checkIsAdmin(request);

        return response.getIsAdmin();
    }
}
