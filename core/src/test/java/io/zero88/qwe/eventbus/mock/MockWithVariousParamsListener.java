package io.zero88.qwe.eventbus.mock;

import java.util.Collection;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBBody;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EBParam;

public class MockWithVariousParamsListener extends MockEventListener {

    @EBContract(action = "NOTIFY")
    public void noReturn(JsonObject data) { }

    @EBContract(action = "GET_LIST")
    public String noParam() { return "hello"; }

    @EBContract(action = "GET_ONE")
    public int javaParam(@EBParam("id") String id) {
        return Integer.parseInt(id);
    }

    @EBContract(action = "PRIMITIVE")
    public long primitive(@EBParam("id") long id) {
        return id;
    }

    @EBContract(action = "CREATE")
    public RequestData refParam(RequestData data) { return data; }

    @EBContract(action = "PATCH")
    public RequestData overrideRefParam(@EBParam("data") RequestData data) { return data; }

    @EBContract(action = "UPDATE")
    public JsonObject twoRefParams(@EBParam("mock") MockParam param, @EBParam("data") RequestData data) {
        return new JsonObject().put("param", JsonObject.mapFrom(param)).put("request", data.toJson());
    }

    @EBContract(action = "REMOVE")
    public JsonObject collectionParam(@EBParam("list") Collection<String> data) {
        JsonObject result = new JsonObject();
        data.forEach(item -> result.put(item, item));
        return result;
    }

    @EBContract(action = "BODY")
    public JsonObject useBodyAndHeader(@EBBody("id") Integer id, @EBParam("headers") JsonObject headers) {
        return new JsonObject().put("id", id).put("headers", headers);
    }
}
