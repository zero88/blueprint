package io.zero88.qwe.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.zero88.exceptions.InvalidUrlException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.JsonHelper;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.exceptions.ServiceNotFoundException;

public class EventMethodDefinitionTest {

    @Test
    public void test_default_wrong_argument_1() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> EventMethodDefinition.createDefault("", ""));
    }

    @Test
    public void test_default_wrong_argument_2() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> EventMethodDefinition.createDefault("/abc", ""));
    }

    @Test
    public void test_same_capture() {
        Assertions.assertThrows(InvalidUrlException.class, () -> EventMethodDefinition.createDefault("abc", "/abc"));
    }

    @Test
    public void test_create_duplicate() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            ActionMethodMapping mapping = () -> {
                Map<EventAction, HttpMethod> test = new HashMap<>();
                test.put(EventAction.GET_LIST, HttpMethod.GET);
                test.put(EventAction.GET_ONE, HttpMethod.GET);
                return test;
            };
            EventMethodDefinition.create("/abc", mapping);
        });
    }

    @Test
    public void test_not_found() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/:id");
            definition.search("/abcd", HttpMethod.GET);
        });
    }

    @Test
    public void test_not_found_multiParam_pattern() {
        Assertions.assertThrows(ServiceNotFoundException.class, () -> {
            EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc/:cid/prod", "/:pid");
            definition.search("/abc/222/xyz/prod", HttpMethod.GET);
        });
    }

    @Test
    public void test_no_capture() {
        ActionMethodMapping mapping = () -> {
            Map<EventAction, HttpMethod> test = new HashMap<>();
            test.put(EventAction.GET_LIST, HttpMethod.GET);
            test.put(EventAction.CREATE, HttpMethod.POST);
            test.put(EventAction.UPDATE, HttpMethod.PUT);
            test.put(EventAction.PATCH, HttpMethod.PATCH);
            test.put(EventAction.REMOVE, HttpMethod.DELETE);
            return test;
        };
        EventMethodDefinition definition = EventMethodDefinition.create("/translate", mapping);
        Assertions.assertEquals(EventAction.GET_LIST, definition.search("/translate", HttpMethod.GET));
        Assertions.assertEquals(EventAction.CREATE, definition.search("/translate", HttpMethod.POST));
        Assertions.assertEquals(EventAction.UPDATE, definition.search("/translate", HttpMethod.PUT));
        Assertions.assertEquals(EventAction.PATCH, definition.search("/translate", HttpMethod.PATCH));
        Assertions.assertEquals(EventAction.REMOVE, definition.search("/translate", HttpMethod.DELETE));
    }

    @Test
    public void test_has_param_but_not_at_last() {
        EventMethodDefinition definition = EventMethodDefinition.create("/p/:pid/data", ActionMethodMapping.by(
            ActionMethodMapping.CRD_MAP,
            Arrays.asList(EventAction.GET_ONE, EventAction.CREATE_OR_UPDATE, EventAction.REMOVE)));
        Assertions.assertEquals(EventAction.GET_ONE, definition.search("/p/123/data", HttpMethod.GET));
        Assertions.assertEquals(EventAction.CREATE_OR_UPDATE, definition.search("/p/123/data", HttpMethod.PUT));
        Assertions.assertEquals(EventAction.REMOVE, definition.search("/p/123/data", HttpMethod.DELETE));
        JsonHelper.assertJson(new JsonObject(
            "{\"regexPath\":\"/p/[^/]+/data\",\"mapping\":[{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/p/:pid/data\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"CREATE_OR_UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/p/:pid/data\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"REMOVE\"," +
            "\"method\":\"DELETE\",\"capturePath\":\"/p/:pid/data\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}]}"), definition.toJson());
    }

    @Test
    public void test_search() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/:id");
        Assertions.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
        Assertions.assertEquals(EventAction.GET_ONE, definition.search("/abc/xyz", HttpMethod.GET));
        Assertions.assertEquals(EventAction.CREATE, definition.search("/abc", HttpMethod.POST));
        Assertions.assertEquals(EventAction.UPDATE, definition.search("/abc/xyz", HttpMethod.PUT));
        Assertions.assertEquals(EventAction.PATCH, definition.search("/abc/xyz", HttpMethod.PATCH));
        Assertions.assertEquals(EventAction.REMOVE, definition.search("/abc/xyz", HttpMethod.DELETE));
        JsonHelper.assertJson(new JsonObject(
            "{\"regexPath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/abc\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc/:id\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/abc/:id\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/abc/:id\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/abc/:id\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}]}"), definition.toJson());
    }

    @Test
    public void test_search_multiParam_pattern_has_resource_between() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/client/:clientId/product",
                                                                               "/:productId");
        Assertions.assertEquals(EventAction.GET_LIST, definition.search("/client/123/product", HttpMethod.GET));
        Assertions.assertEquals(EventAction.GET_ONE, definition.search("/client/123/product/456", HttpMethod.GET));
        Assertions.assertEquals(EventAction.CREATE, definition.search("/client/123/product", HttpMethod.POST));
        Assertions.assertEquals(EventAction.UPDATE, definition.search("/client/123/product/456", HttpMethod.PUT));
        Assertions.assertEquals(EventAction.PATCH, definition.search("/client/123/product/456", HttpMethod.PATCH));
        Assertions.assertEquals(EventAction.REMOVE, definition.search("/client/123/product/456", HttpMethod.DELETE));
        JsonHelper.assertJson(new JsonObject(
            "{\"regexPath\":\"/client/[^/]+/product\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/client/:clientId/product\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/client/:clientId/product\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\"," +
            "\"method\":\"GET\",\"capturePath\":\"/client/:clientId/product/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"UPDATE\",\"method\":\"PUT\"," +
            "\"capturePath\":\"/client/:clientId/product/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/client/:clientId/product/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"REMOVE\",\"method\":\"DELETE\"," +
            "\"capturePath\":\"/client/:clientId/product/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}]}"), definition.toJson());
    }

    @Test
    public void test_wrong_multiParam_pattern() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> EventMethodDefinition.createDefault("/client/:clientId", "/:productId"));
    }

    @Test
    public void test_search_multiParam_pattern_no_resource_between() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/client/:clientId/", "/:productId");
        Assertions.assertEquals(EventAction.GET_LIST, definition.search("/client/123/", HttpMethod.GET));
        Assertions.assertEquals(EventAction.GET_ONE, definition.search("/client/123/456", HttpMethod.GET));
        Assertions.assertEquals(EventAction.CREATE, definition.search("/client/123/", HttpMethod.POST));
        Assertions.assertEquals(EventAction.UPDATE, definition.search("/client/123/456", HttpMethod.PUT));
        Assertions.assertEquals(EventAction.PATCH, definition.search("/client/123/456", HttpMethod.PATCH));
        Assertions.assertEquals(EventAction.REMOVE, definition.search("/client/123/456", HttpMethod.DELETE));
        JsonObject expected = new JsonObject(
            "{\"regexPath\":\"/client/[^/]+/\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/client/:clientId/\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/client/:clientId/\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\"," +
            "\"method\":\"GET\",\"capturePath\":\"/client/:clientId/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"UPDATE\",\"method\":\"PUT\"," +
            "\"capturePath\":\"/client/:clientId/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/client/:clientId/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"REMOVE\",\"method\":\"DELETE\"," +
            "\"capturePath\":\"/client/:clientId/:productId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}]}");
        JsonHelper.assertJson(expected, definition.toJson());
    }

    @Test
    public void test_to_json() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/abc", "/:id");
        JsonObject expected = new JsonObject(
            "{\"regexPath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/abc\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/abc/:id\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/abc/:id\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\",\"method\":\"PATCH\"," +
            "\"capturePath\":\"/abc/:id\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/abc/:id\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}}]}");
        JsonHelper.assertJson(expected, definition.toJson());
    }

    @Test
    public void test_to_json_multiParams() {
        EventMethodDefinition definition = EventMethodDefinition.createDefault("/c/:cId/p", "/:pId");
        JsonObject expected = new JsonObject(
            "{\"regexPath\":\"/c/[^/]+/p\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"capturePath\":\"/c/:cId/p\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}}," +
            "{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/c/:cId/p\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"GET_ONE\",\"method\":\"GET\"," +
            "\"capturePath\":\"/c/:cId/p/:pId\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}},{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/c/:cId/p/:pId\"," +
            "\"useRequestData\":true,\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"PATCH\"," +
            "\"method\":\"PATCH\",\"capturePath\":\"/c/:cId/p/:pId\",\"useRequestData\":true," +
            "\"auth\":{\"loginRequired\":false,\"authz\":[]}},{\"action\":\"REMOVE\",\"method\":\"DELETE\"," +
            "\"capturePath\":\"/c/:cId/p/:pId\",\"useRequestData\":true,\"auth\":{\"loginRequired\":false," +
            "\"authz\":[]}}]}");
        JsonHelper.assertJson(expected, definition.toJson());
    }

    @Test
    public void test_from_json() {
        EventMethodDefinition definition = JsonData.from(
            "{\"regexPath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\"," +
            "\"method\":\"GET\",\"capturePath\":\"/abc\"},{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath" +
            "\":\"/abc\"},{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/abc/:id\"}," +
            "{\"action\":\"GET_ONE\",\"method\":\"GET\",\"capturePath\":\"/abc/:id\"}," +
            "{\"action\":\"PATCH\",\"method\":\"PATCH\",\"capturePath\":\"/abc/:id\"}," +
            "{\"action\":\"REMOVE\",\"method\":\"DELETE\",\"capturePath\":\"/abc/:id\"}]}",
            EventMethodDefinition.class);
        System.out.println(definition.toJson());
        Assertions.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
        Assertions.assertEquals(EventAction.GET_ONE, definition.search("/abc/xyz", HttpMethod.GET));
        Assertions.assertEquals(EventAction.CREATE, definition.search("/abc", HttpMethod.POST));
        Assertions.assertEquals(EventAction.UPDATE, definition.search("/abc/xyz", HttpMethod.PUT));
        Assertions.assertEquals(EventAction.PATCH, definition.search("/abc/xyz", HttpMethod.PATCH));
        Assertions.assertEquals(EventAction.REMOVE, definition.search("/abc/xyz", HttpMethod.DELETE));
    }

    @Test
    public void test_from_json_not_use_request_data() {
        EventMethodDefinition definition = JsonData.from(
            "{\"regexPath\":\"/abc\",\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\"," +
            "\"useRequestData\":false,\"capturePath\":\"/abc\"}]}", EventMethodDefinition.class);
        Assertions.assertEquals(EventAction.GET_LIST, definition.search("/abc", HttpMethod.GET));
    }

    @Test
    public void test_search_similar_pattern() {
        EventMethodDefinition definition1 = EventMethodDefinition.createDefault("/device/:device_id/folder",
                                                                                "/:folder_id");
        EventMethodDefinition definition2 = EventMethodDefinition.createDefault(
            "/device/:device_id/folder/:folder_id/point", "/:point_id");
        Assertions.assertEquals(EventAction.GET_ONE,
                                definition2.search("/device/123/folder/345/point/abc", HttpMethod.GET));
        Assertions.assertEquals(EventAction.GET_LIST,
                                definition2.search("/device/123/folder/345/point", HttpMethod.GET));
        Assertions.assertThrows(ServiceNotFoundException.class,
                                () -> definition1.search("/device/123/folder/345/point", HttpMethod.GET));
    }

}
