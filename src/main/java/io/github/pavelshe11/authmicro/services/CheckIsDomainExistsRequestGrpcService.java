package io.github.pavelshe11.authmicro.services;

import io.github.pavelshe11.authmicro.grpc.CheckIsDomainExistsProto;
import io.github.pavelshe11.authmicro.grpc.CheckIsDomainExistsServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class CheckIsDomainExistsRequestGrpcService {
    @GrpcClient("check-domain-service")
    private CheckIsDomainExistsServiceGrpc.CheckIsDomainExistsServiceBlockingStub domainCheckStub;

    public boolean checkIsDomainExists(String domain) {
        CheckIsDomainExistsProto.CheckIsDomainExistsRequest request =
                CheckIsDomainExistsProto.CheckIsDomainExistsRequest.newBuilder()
                .setDomain(domain)
                .build();

        CheckIsDomainExistsProto.CheckIsDomainExistsResponse response =
                domainCheckStub.checkIsDomainExists(request);

        return response.getExists();
    }

}
