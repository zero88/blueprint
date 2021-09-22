package io.zero88.qwe.eventbus.mock;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EBParam;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.exceptions.TimeoutException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockFutureListener implements EventListener {

    @EBContract(action = "GET_ONE")
    public Future<JsonObject> receive(@EBParam("id") int id) {
        return Future.succeededFuture(new JsonObject().put("resp", id));
    }

    @EBContract(action = "CREATE")
    public Future<Void> voidFuture(@EBParam("id") int id) {
        return Future.succeededFuture();
    }

    @EBContract(action = "ERR")
    public Future<Void> failedFuture(@EBParam("id") int id) {
        return Future.failedFuture(new TimeoutException("alo"));
    }

}
