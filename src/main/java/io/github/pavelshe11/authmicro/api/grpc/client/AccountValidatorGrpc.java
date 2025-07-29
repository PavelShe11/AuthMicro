package io.github.pavelshe11.authmicro.api.grpc.client;
import com.google.protobuf.NullValue;
import io.github.pavelshe11.authmicro.api.http.server.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.grpc.AccountValidatorProto;
import net.devh.boot.grpc.client.inject.GrpcClient;

import io.github.pavelshe11.authmicro.grpc.AccountValidatorServiceGrpc;
import com.google.protobuf.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountValidatorGrpc {
    @GrpcClient("account-validator-service")
    private AccountValidatorServiceGrpc.AccountValidatorServiceBlockingStub accountValidatorServiceBlockingStub;

    public AccountValidatorProto.ValidateUserDataResponse validateUserData(Map<String, Object> userData) {
        Map<String, Value> userDataValueMap = convertToProtoValueMap(userData);

        AccountValidatorProto.ValidateUserDataRequest request = AccountValidatorProto.ValidateUserDataRequest.newBuilder()
                .putAllUserData(userDataValueMap)
                .build();

        return accountValidatorServiceBlockingStub.validateUserData(request);
    }

    private Map<String, Value> convertToProtoValueMap(Map<String, Object> userData) {
        return userData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> convertObjectToProtoValue(e.getValue())
                ));
    }

    private Value convertObjectToProtoValue(Object value) {
        Value.Builder valueBuilder = Value.newBuilder();
        switch (value) {
            case null -> valueBuilder.setNullValue(NullValue.NULL_VALUE);
            case Boolean b -> valueBuilder.setBoolValue(b);
            case Number number -> valueBuilder.setNumberValue(number.doubleValue());
            case String s -> valueBuilder.setStringValue(s);
            default -> throw new ServerAnswerException("Сервер не отвечает.");
        }
        return valueBuilder.build();
    }
}
