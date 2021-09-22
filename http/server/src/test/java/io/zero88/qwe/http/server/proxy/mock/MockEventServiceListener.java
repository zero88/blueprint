package io.zero88.qwe.http.server.proxy.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBBody;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.exceptions.DataNotFoundException;

import lombok.RequiredArgsConstructor;

class MockEventServiceListener {

    static String TEST_EVENT_1_ADDR = "test.MockEventMessageService.1";
    static String TEST_EVENT_2_ADDR = "test.MockEventMessageService.2";
    static String TEST_EVENT_3_ADDR = "test.MockEventMessageService.3";
    static String TEST_EVENT_4_ADDR = "test.MockEventMessageService.4";
    static final EventListener TEST_EVENT_LISTENER_1 = new SimpleEventListener();
    static final EventListener TEST_EVENT_LISTENER_2 = new MultiParamEventListener();
    static final EventListener TEST_EVENT_LISTENER_3 = new MultiParamNotUseRequestDataEventListener();
    static final EventListener TEST_EVENT_LISTENER_4 = new MultiApiPathEventListener();


    @RequiredArgsConstructor
    static class SimpleEventListener implements EventListener {

        @EBContract(action = "GET_LIST")
        public List<String> list() { return Arrays.asList("1", "2", "3"); }

        @EBContract(action = "GET_ONE")
        public int get(RequestData data) { return Integer.parseInt(data.body().getString("id")); }

    }


    @RequiredArgsConstructor
    static class MultiParamEventListener implements EventListener {

        @EBContract(action = "GET_LIST")
        public List<String> list(RequestData data) { return Collections.singletonList(data.body().getString("cId")); }

        @EBContract(action = "GET_ONE")
        public JsonObject get(RequestData data) {
            return data.body();
        }

    }


    @RequiredArgsConstructor
    static class MultiApiPathEventListener implements EventListener {

        private static final JsonObject CID_01 = new JsonObject().put("cId.01", new JsonArray().add(
            new JsonObject().put("pId.01", "xxx")).add(new JsonObject().put("pId.02", "abc")));
        private static final JsonObject CID_02 = new JsonObject().put("cId.02", new JsonArray().add(
            new JsonObject().put("pId.03", "123")).add(new JsonObject().put("pId.04", "456")));
        private static final JsonArray DATA = new JsonArray().add(CID_01).add(CID_02);

        @EBContract(action = "GET_LIST")
        public List<Object> list(RequestData data) {
            final String cId = data.body().getString("cId");
            return DATA.stream()
                       .map(JsonObject.class::cast)
                       .filter(s -> Strings.isBlank(cId) || s.containsKey(cId))
                       .map(JsonObject::stream)
                       .flatMap(s -> s.map(Entry::getValue))
                       .map(JsonArray.class::cast)
                       .flatMap(JsonArray::stream)
                       .collect(Collectors.toList());
        }

        @EBContract(action = "GET_ONE")
        public JsonObject get(RequestData data) {
            final String cId = data.body().getString("cId");
            final String pId = data.body().getString("pId");
            return DATA.stream()
                       .map(JsonObject.class::cast)
                       .filter(c -> Strings.isBlank(cId) || c.containsKey(cId))
                       .map(JsonObject::stream)
                       .flatMap(s -> s.map(Entry::getValue))
                       .map(JsonArray.class::cast)
                       .flatMap(JsonArray::stream)
                       .map(JsonObject.class::cast)
                       .filter(p -> !Strings.isBlank(pId) && p.containsKey(pId))
                       .findFirst()
                       .orElseThrow(() -> new DataNotFoundException("Not found"));
        }

    }


    @RequiredArgsConstructor
    static class MultiParamNotUseRequestDataEventListener implements EventListener {

        @EBContract(action = "GET_LIST")
        public List<String> list(@EBBody("xId") String xId) {
            return Collections.singletonList(xId);
        }

        @EBContract(action = "GET_ONE")
        public JsonObject get(@EBBody("xId") String xId, @EBBody("yId") String yId) {
            return new JsonObject().put("xId", xId).put("yId", yId);
        }

    }

}
