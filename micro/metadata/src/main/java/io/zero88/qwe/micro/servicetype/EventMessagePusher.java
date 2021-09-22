package io.zero88.qwe.micro.servicetype;

import io.vertx.core.Future;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventAction;
import io.zero88.qwe.eventbus.EventMessage;

public interface EventMessagePusher {

    /**
     * Push data via Event Bus then consume reply data
     *
     * @param action      Event action
     * @param requestData Request Data
     * @return a future
     */
    Future<EventMessage> execute(EventAction action, RequestData requestData);

}
