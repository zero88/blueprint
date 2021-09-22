package io.zero88.qwe.eventbus;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.LogSystem;
import io.zero88.qwe.SharedDataLocalProxy;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
final class EventBusClientImpl implements EventBusClient, HasLogger, LogSystem {

    @Getter
    @NonNull
    @Accessors(fluent = true)
    private final SharedDataLocalProxy sharedData;
    private final Class<EventBusReplyHandler> replyHandlerClass;

    @Override
    public Logger logger() {
        return LoggerFactory.getLogger(EventBusClient.class);
    }

    @Override
    @NonNull
    public String function() {
        return "EventBus";
    }

    @Override
    public EventBusClient send(@NonNull String address, @NonNull EventMessage message, DeliveryOptions options) {
        unwrap().send(address, message.toJson(), getOpts(options));
        return this;
    }

    @Override
    public Future<EventMessage> request(@NonNull String address, @NonNull EventMessage message,
                                        DeliveryOptions options) {
        final EventBusReplyHandler replyHandler = EventBusReplyHandler.create(replyHandlerClass);
        return unwrap().request(address, message.toJson(), getOpts(options))
                       .map(replyHandler::succeed)
                       .otherwise(t -> replyHandler.error(message.getAction(), t))
                       .onSuccess(msg -> logger().info(decor("Response [{}][{}=>{}][{}]"), address, msg.getAction(),
                                                       msg.getPrevAction(), msg.getStatus()));
    }

    @Override
    public EventBusClient publish(@NonNull String address, @NonNull EventMessage message, DeliveryOptions options) {
        unwrap().publish(address, message.toJson(), getOpts(options));
        return this;
    }

    @Override
    public EventBusClient register(String address, boolean local, @NonNull EventListener listener) {
        logger().info(decor("Register [{}][{}][{}]"), Strings.requireNotBlank(address), listener.getClass().getName(),
                      local ? "Local" : "Cluster");
        if (local) {
            unwrap().localConsumer(address, msg -> listener.handle(sharedData, msg));
        } else {
            unwrap().consumer(address, msg -> listener.handle(sharedData, msg));
        }
        return this;
    }

    DeliveryOptions getOpts(DeliveryOptions opts) {
        if (Objects.nonNull(opts)) {
            return opts;
        }
        final EventBusDeliveryOption option = sharedData.getData(SharedDataLocalProxy.EVENTBUS_DELIVERY_OPTION_KEY);
        return Optional.ofNullable(option).map(EventBusDeliveryOption::get).orElseGet(DeliveryOptions::new);
    }

}
