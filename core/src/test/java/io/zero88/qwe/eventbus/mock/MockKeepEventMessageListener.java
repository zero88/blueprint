package io.zero88.qwe.eventbus.mock;

import java.util.Objects;

import io.vertx.core.json.JsonObject;
import io.zero88.qwe.dto.ErrorMessage;
import io.zero88.qwe.eventbus.EBContract;
import io.zero88.qwe.eventbus.EBParam;
import io.zero88.qwe.eventbus.EventListener;

public class MockKeepEventMessageListener implements EventListener {

    @EBContract(action = "MONITOR")
    public int monitor(@EBParam("data") JsonObject json, @EBParam("error") ErrorMessage err) {
        return Objects.isNull(err) ? 1 : 0;
    }

    @EBContract(action = "NOTIFY")
    public int swap(@EBParam("error") ErrorMessage error, @EBParam("data") JsonObject data) {
        return Objects.isNull(error) ? 1 : 0;
    }

}
