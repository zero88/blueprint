package io.zero88.qwe.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.JsonHelper;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.eventbus.EventAction;

class EventMethodMappingTest {

    @Test
    public void serialize() {
        EventMethodMapping mapping = new EventMethodMapping(EventAction.CREATE, HttpMethod.CONNECT, "/abc");
        JsonHelper.assertJson(new JsonObject(
            "{\"action\":\"CREATE\",\"capturePath\":\"/abc\",\"useRequestData\":true," +
            "\"method\":\"CONNECT\",\"auth\":{\"loginRequired\":false,\"authz\":[]}}"), mapping.toJson());
    }

    @Test
    public void deserialize() {
        final EventMethodMapping from = JsonData.from(
            "{\"action\":\"REMOVE\",\"capturePath\":\"/xyz\",\"regexPath\":\"/xyz\",\"method\":\"DELETE\"}",
            EventMethodMapping.class);
        final EventMethodMapping expected = new EventMethodMapping(EventAction.REMOVE, HttpMethod.DELETE, "/xyz");
        Assertions.assertEquals(expected, from);
        Assertions.assertEquals(expected.getRegexPath(), from.getRegexPath());
        JsonHelper.assertJson(expected.toJson(), from.toJson());
    }

    @Test
    void test() {
        Assertions.assertEquals("/c/[^/]+/p/[^/]+", new EventMethodMapping(EventAction.CREATE, HttpMethod.POST,
                                                                           "/c/:cId/p/:pId").getRegexPath());
        Assertions.assertEquals("/c/[^/]+/p/[^/]+/", new EventMethodMapping(EventAction.CREATE, HttpMethod.POST,
                                                                           "/c/:cId/p/:pId/").getRegexPath());
        Assertions.assertEquals("/c/[^/]+/p", new EventMethodMapping(EventAction.GET_ONE, HttpMethod.GET,
                                                                     "/c/:cId/p").getRegexPath());
        Assertions.assertEquals("/c/[^/]+/p/", new EventMethodMapping(EventAction.GET_ONE, HttpMethod.GET,
                                                                     "/c/:cId/p/").getRegexPath());
    }

}
