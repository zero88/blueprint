package io.zero88.qwe.http.server.mock;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;

public class MockEventSuccessListener implements EventListener {

    @EBContract(action = "GET_LIST")
    public List<String> list(RequestData data) {
        return Arrays.asList("1", "2", "3");
    }

    @EBContract(action = "GET_ONE")
    public int get(RequestData data) {
        return Integer.parseInt(data.body().getString("event_id"));
    }

    @EBContract(action = "CREATE")
    public JsonObject create(RequestData data) {
        return new JsonObject().put("create", "success");
    }

    @EBContract(action = "UPDATE")
    public Future<String> update(RequestData data) {
        return Future.succeededFuture("success");
    }

    @EBContract(action = "PATCH")
    public Future<JsonObject> patch(RequestData data) {
        return Future.succeededFuture(new JsonObject().put("patch", "success")
                                                      .put("event_id",
                                                           Integer.valueOf(data.body().getString("event_id"))));
    }

}
