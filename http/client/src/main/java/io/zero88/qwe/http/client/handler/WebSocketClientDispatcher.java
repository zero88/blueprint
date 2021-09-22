package io.zero88.qwe.http.client.handler;

import java.util.Objects;

import io.github.zero88.repl.Arguments;
import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.dto.JsonData;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventDirection;
import io.zero88.qwe.eventbus.EventMessage;
import io.zero88.qwe.http.client.HttpClientLog.WebSocketLog;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Handle light Websocket response then dispatch based on Event Listener
 */
@RequiredArgsConstructor
public abstract class WebSocketClientDispatcher implements Handler<Buffer>, HasLogger, WebSocketLog {

    @NonNull
    private final EventBusClient eventbus;
    @NonNull
    private final EventDirection listener;

    @SuppressWarnings("unchecked")
    public static <T extends WebSocketClientDispatcher> T create(@NonNull EventBusClient client,
                                                                 @NonNull EventDirection listener,
                                                                 Class<T> bodyHandlerClass) {
        if (Objects.isNull(bodyHandlerClass) || WebSocketClientDispatcher.class.equals(bodyHandlerClass)) {
            return (T) new WebSocketClientDispatcher(client, listener) {};
        }
        return ReflectionClass.createObject(bodyHandlerClass, new Arguments().put(EventBusClient.class, client)
                                                                             .put(EventDirection.class, listener));
    }

    @Override
    public void handle(Buffer data) {
        logger().info(decor("Dispatch message to [{}]"), listener.getAddress());
        eventbus.fire(listener.getAddress(), listener.getPattern(),
                      EventMessage.tryParse(JsonData.tryParse(data), true));
    }

}
