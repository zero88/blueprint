package io.zero88.qwe.auth;

import java.util.Collection;
import java.util.HashSet;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.EnumType;
import io.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.zero88.qwe.dto.JsonData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

public interface Credential extends JsonData {

    @JsonUnwrapped
    CredentialType getType();

    String getUser();

    String getHeaderAuthType();

    String secretValue();

    final class CredentialType extends AbstractEnumType {

        public static final CredentialType BASIC = new CredentialType("BASIC");
        public static final CredentialType TOKEN = new CredentialType("TOKEN");

        private CredentialType(String type) {
            super(type);
        }

        @JsonCreator
        public static CredentialType factory(String name) {
            return EnumType.factory(name, CredentialType.class, true);
        }

    }


    @Getter
    @SuperBuilder
    abstract class AbstractCredential implements Credential {

        protected final String user;
        protected CredentialType type;

        protected abstract Collection<String> sensitiveFields();

        @Override
        public String toString() {
            return "Type[" + getType() + "]::User[" + user + "]::" + maskSensitive();
        }

        @Override
        public JsonObject toJson(@NonNull ObjectMapper mapper) {
            return toJson(mapper, new HashSet<>(sensitiveFields()));
        }

        @JsonProperty("type")
        protected AbstractCredential type(String type) {
            this.type = CredentialType.factory(type);
            return this;
        }

        protected abstract String maskSensitive();

    }

}
