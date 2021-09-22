package io.zero88.qwe.exceptions;

import java.util.Objects;

import io.zero88.qwe.dto.EnumType;
import io.zero88.qwe.dto.EnumType.AbstractEnumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ErrorCode extends AbstractEnumType implements io.github.zero88.exceptions.ErrorCode, EnumType {

    public static final ErrorCode DESIRED_ERROR = parse("DESIRED_ERROR");
    public static final ErrorCode CONFLICT_ERROR = parse("CONFLICT_ERROR");

    public static final ErrorCode DATA_ALREADY_EXIST = parse("DATA_ALREADY_EXIST");
    public static final ErrorCode DATA_BEING_USED = parse("DATA_BEING_USED");
    public static final ErrorCode DATA_NOT_FOUND = parse("DATA_NOT_FOUND");

    public static final ErrorCode ENGINE_ERROR = parse("ENGINE_ERROR");

    public static final ErrorCode NETWORK_ERROR = parse("NETWORK_ERROR");

    public static final ErrorCode INITIALIZER_ERROR = parse("INITIALIZER_ERROR");

    public static final ErrorCode SERVICE_ERROR = parse("SERVICE_ERROR");
    public static final ErrorCode SERVICE_UNAVAILABLE = parse("SERVICE_UNAVAILABLE");
    public static final ErrorCode SERVICE_NOT_FOUND = parse("SERVICE_NOT_FOUND");

    public static final ErrorCode SECURITY_ERROR = parse("SECURITY_ERROR");
    public static final ErrorCode AUTHENTICATION_ERROR = parse("AUTHENTICATION_ERROR");
    public static final ErrorCode INSUFFICIENT_PERMISSION_ERROR = parse("INSUFFICIENT_PERMISSION_ERROR");

    public static final ErrorCode TIMEOUT_ERROR = parse("TIMEOUT_ERROR");

    @JsonCreator
    private ErrorCode(String code) {
        super(code);
    }

    @Override
    @JsonValue
    public @NonNull String code() {
        return type();
    }

    public static ErrorCode parse(String code) {
        return EnumType.factory(code, ErrorCode.class, true);
    }

    public static ErrorCode wrap(io.github.zero88.exceptions.ErrorCode code) {
        return code instanceof ErrorCode ? (ErrorCode) code : ErrorCode.parse(code.code());
    }

    public int hashCode() {
        return this.code().hashCode();
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof String) {
            return Objects.equals(this.code(), o);
        }
        if (!(o instanceof io.github.zero88.exceptions.ErrorCode)) {
            return false;
        }
        return Objects.equals(this.code(), ((io.github.zero88.exceptions.ErrorCode) o).code());
    }

}
