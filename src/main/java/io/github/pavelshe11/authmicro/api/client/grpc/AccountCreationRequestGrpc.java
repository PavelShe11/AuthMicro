package io.github.pavelshe11.authmicro.api.client.grpc;

import com.google.protobuf.NullValue;
import com.google.protobuf.Value;
import io.github.pavelshe11.authmicro.api.exceptions.ServerAnswerException;
import io.github.pavelshe11.authmicro.grpc.AccountCreationProto;
import io.github.pavelshe11.authmicro.grpc.AccountCreationServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountCreationRequestGrpc {
    @GrpcClient("account-creation-service")
    private AccountCreationServiceGrpc.AccountCreationServiceBlockingStub accountCreationServiceBlockingStub;

    public boolean createAccount(Map<String, Object> userData) {
        try {
            Map<String, Value> userDataValueMap = convertToProtoValueMap(userData);

            AccountCreationProto.CreateAccountRequest request =
                    AccountCreationProto.CreateAccountRequest.newBuilder()
                            .putAllUserData(userDataValueMap)
                            .build();

            AccountCreationProto.CreateAccountResponse response =
                    accountCreationServiceBlockingStub.createAccount(request);

            return response.hasSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Value> convertToProtoValueMap(Map<String, Object> userData) {
        return userData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> convertObjectToProtoValue(entry.getValue())
                ));
    }

    private Value convertObjectToProtoValue(Object value) {
        Value.Builder builder = Value.newBuilder();
        return switch (value) {
            case null -> builder.setNullValue(NullValue.NULL_VALUE).build();
            case String s -> builder.setStringValue(s).build();
            case Boolean b -> builder.setBoolValue(b).build();
            case Number n -> builder.setNumberValue(n.doubleValue()).build();
            default -> throw new ServerAnswerException();
        };
    }
}
