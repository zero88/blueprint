package io.zero88.qwe.http.server.handler;

import io.vertx.core.Future;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.eventbus.EventPattern;
import io.zero88.qwe.exceptions.TimeoutException;
import io.zero88.qwe.http.server.HttpSystem.ApisSystem;

public interface EventBusProxyDispatcher extends RequestDispatcher<RequestData, EventMessage>, HasLogger, ApisSystem {

    default Future<EventMessage> doDispatch(EventBusClient client, String address, EventPattern pattern,
                                            EventMessage message) {
        logger().info(decor("Dispatch HTTP request to address[{}::{}]"), message.getAction(), address);
        return client.fire(address, pattern, message);
    }

    @Override
    default ResponseInterceptor<EventMessage> responseInterceptor() {
        return new ResponseEventInterceptor();
    }

}
