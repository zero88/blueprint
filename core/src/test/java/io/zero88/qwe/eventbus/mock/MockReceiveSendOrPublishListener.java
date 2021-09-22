package io.zero88.qwe.eventbus.mock;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EBParam;
import io.zero88.qwe.eventbus.EventListener;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockReceiveSendOrPublishListener implements EventListener {

    private final String identifier;
    private final Checkpoint cp;

    @EBContract(action = "GET_ONE")
    public JsonObject receive(@EBParam("id") int id) {
        cp.flag();
        System.out.println("[" + identifier + "] receive event");
        return new JsonObject().put("id", id).put("identifier", identifier);
    }

}
