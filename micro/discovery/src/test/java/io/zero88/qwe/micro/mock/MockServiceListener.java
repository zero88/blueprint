package io.zero88.qwe.micro.mock;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.micro.GatewayHeaders;

public final class MockServiceListener implements EventListener {

    @EBContract(action = "CREATE")
    public JsonObject create(RequestData requestData) {
        return new JsonObject().put("action", EventAction.CREATE.action())
                               .put(GatewayHeaders.X_REQUEST_BY,
                                    requestData.headers().getString(GatewayHeaders.X_REQUEST_BY));
    }

    @EBContract(action = "UPDATE")
    public JsonObject update(RequestData requestData) {
        throw new IllegalArgumentException("hey");
    }

}
