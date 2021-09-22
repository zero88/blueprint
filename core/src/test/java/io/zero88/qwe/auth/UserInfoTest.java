package io.zero88.qwe.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

class UserInfoTest {

    @Test
    void test_serialize_deserialize() {
        final JsonObject json = new JsonObject().put("identifier", "123").put("name", "zero88");
        final UserInfo userInfo = UserInfo.create(json);
        Assertions.assertEquals("123", userInfo.identifier());
        Assertions.assertEquals("zero88", userInfo.get("name"));
        Assertions.assertNull(userInfo.get("age"));
        Assertions.assertEquals(json, userInfo.toJson());
    }

    @Test
    void test_serialize_deserialize_custom() {
        final JsonObject json = new JsonObject().put("id", "123").put("name", "zero88");
        final UserInfo userInfo = UserInfo.create(json, "id");
        Assertions.assertEquals("123", userInfo.identifier());
        Assertions.assertEquals("zero88", userInfo.get("name"));
        Assertions.assertNull(userInfo.get("age"));
        Assertions.assertEquals(json.put(UserInfo.IDENTITY_KEY, "123"), userInfo.toJson());
    }

}
