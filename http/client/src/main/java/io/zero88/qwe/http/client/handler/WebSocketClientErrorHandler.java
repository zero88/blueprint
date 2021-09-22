package io.zero88.qwe.http.client.handler;

import java.util.Objects;

import io.github.zero88.repl.Arguments;
import io.github.zero88.repl.ReflectionClass;
import io.vertx.core.Handler;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.eventbus.EventBusClient;
import io.zero88.qwe.eventbus.EventDirection;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class WebSocketClientErrorHandler implements Handler<Throwable>, HasLogger {

    @NonNull
    private final EventBusClient client;
    @NonNull
    private final EventDirection listener;

    @SuppressWarnings("unchecked")
    public static <T extends WebSocketClientErrorHandler> T create(@NonNull EventBusClient client,
                                                                   @NonNull EventDirection listener,
                                                                   @NonNull Class<T> errorHandlerClass) {
        if (Objects.isNull(errorHandlerClass) || WebSocketClientErrorHandler.class.equals(errorHandlerClass)) {
            return (T) new IgnoreWebSocketClientError(client, listener);
        }
        return ReflectionClass.createObject(errorHandlerClass, new Arguments().put(EventBusClient.class, client)
                                                                              .put(EventDirection.class, listener));
    }

    public static class IgnoreWebSocketClientError extends WebSocketClientErrorHandler {

        IgnoreWebSocketClientError(@NonNull EventBusClient controller, @NonNull EventDirection listener) {
            super(controller, listener);
        }

        @Override
        public void handle(Throwable error) {
            logger().warn("Error in websocket response", error);
        }

    }

}
