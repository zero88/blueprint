package io.zero88.qwe.auth;

import java.util.Optional;

import io.vertx.core.json.JsonObject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
public class UserInfoImpl implements UserInfo {

    @Getter
    private final String identifier;
    private final JsonObject extra;

    @Override
    public Object get(String key) {
        return Optional.ofNullable(extra).map(json -> json.getValue(key)).orElse(null);
    }

    @Override
    public JsonObject toJson() {
        JsonObject o = extra == null ? new JsonObject() : new JsonObject(extra.getMap());
        return o.put("identity", identifier);
    }

}
