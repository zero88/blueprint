package io.zero88.qwe.http.server.mock;

import io.github.zero88.exceptions.ErrorCode;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EventListener;
import io.zero88.qwe.exceptions.EngineException;
import io.zero88.qwe.exceptions.QWEException;

public class MockEventErrorListener implements EventListener {

    @EBContract(action = "GET_LIST")
    public JsonObject list(RequestData data) {
        throw new RuntimeException("xxx");
    }

    @EBContract(action = "CREATE")
    public JsonObject create(RequestData data) {
        throw new EngineException("Engine error");
    }

    @EBContract(action = "UPDATE")
    public JsonObject update(RequestData data) {
        throw new QWEException(ErrorCode.INVALID_ARGUMENT, "invalid");
    }

}
