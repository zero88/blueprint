package io.zero88.qwe.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.zero88.utils.Strings;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.JsonData;

/**
 * Represents for a lean user information to transmit among services
 */
public interface UserInfo extends JsonData {

    /**
     * The constant key is used in {@code Vertx local context data}
     */
    String USER_KEY = "USER_INFO";
    /**
     * Identity key for defining user identification
     */
    String IDENTITY_KEY = "identifier";

    static UserInfo parse(Object user) {
        if (user == null) {
            return null;
        }
        if (user instanceof UserInfo) {
            return (UserInfo) user;
        }
        if (user instanceof JsonObject) {
            return create((JsonObject) user);
        }
        if (user instanceof Buffer) {
            return create(((Buffer) user).toJsonObject());
        }
        throw new IllegalArgumentException("Unable parse user");
    }

    static UserInfo create(JsonObject user) {
        return create(user, IDENTITY_KEY);
    }

    static UserInfo create(JsonObject user, String identityKey) {
        return create(user.getString(identityKey), user);
    }

    static UserInfo create(String identifier, JsonObject extraInfo) {
        return new UserInfoImpl(Strings.requireNotBlank(identifier, "User identifier is missing"), extraInfo);
    }

    /**
     * @return user identity
     */
    @NotNull String identifier();

    /**
     * Get extra property by key
     *
     * @param key property key
     * @return property value, it is nullable
     */
    @Nullable Object get(String key);

}
